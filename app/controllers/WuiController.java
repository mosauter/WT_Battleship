package controllers;// WuiController

import controllers.util.FirstPlayerMessage;
import controllers.util.HitMessage;
import controllers.util.InvalidMessage;
import controllers.util.Message;
import controllers.util.PlaceErrorMessage;
import controllers.util.PlaceMessage;
import controllers.util.ShootMessage;
import controllers.util.WaitMessage;
import controllers.util.WinMessage;
import de.htwg.battleship.controller.IMasterController;
import de.htwg.battleship.model.IPlayer;
import de.htwg.battleship.observer.IObserver;
import de.htwg.battleship.util.StatCollection;
import de.htwg.battleship.util.State;
import play.Logger;
import play.mvc.WebSocket;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Semaphore;

/**
 * WuiController
 *
 * @author ms
 * @since 2015-11-25
 */
public class WuiController implements IObserver {

    private static final String HORIZONTAL_ORIENTATION = "true";

    private final IMasterController masterController;
    private final WebSocket.Out<String> socket;
    private final boolean firstPlayer;
    private final List<String[]> bufferedPlaceList;
    private final List<String[]> bufferedShootList;
    private boolean placeOneFinished = false;

    private Semaphore addShip;

    public WuiController(IMasterController masterController, WebSocket.Out<String> socket, boolean first) {
        this.masterController = masterController;
        this.socket = socket;
        this.firstPlayer = first;
        this.bufferedPlaceList = new LinkedList<>();
        this.bufferedShootList = new LinkedList<>();
        masterController.addObserver(this);
        this.send(new FirstPlayerMessage(first));
        this.addShip = new Semaphore(1);
    }

    private void send(Message msg) {
        if (msg != null) {
            Logger.debug("Sending message:\n" + msg.toJSON());
            socket.write(msg.toJSON());
        }
    }

    public void analyzeMessage(String message) {
        Logger.info("Received message:\n" + message);
        String[] field = message.split(" ");
        if (field.length == 3) {
            // x y orientation -> which player
            placeShip(field);
        } else if (field.length == 2) {
            // x y -> test which player
            shoot(field);
        } else {
            send(new InvalidMessage());
        }
    }

    private boolean processPlaceList() {
        if (bufferedPlaceList.isEmpty()) {
            return false;
        }
        String[] instruction = bufferedPlaceList.get(0);
        bufferedPlaceList.remove(0);
        placeShip(instruction);
        return true;
    }

    private void placeShip(String[] field) {
        System.out.println("adding ship -> " + (firstPlayer && masterController
            .getCurrentState()
            .equals(State.PLACE1) || !firstPlayer && masterController
            .getCurrentState().equals(State.PLACE2)));
        if (firstPlayer && masterController.getCurrentState()
                                           .equals(State.PLACE1) || !firstPlayer && masterController
            .getCurrentState().equals(State.PLACE2)) {
            try {
                addShip.acquire();
                masterController.placeShip(Integer.parseInt(field[0]), Integer
                    .parseInt(field[1]), field[2]
                    .equals(HORIZONTAL_ORIENTATION));
                addShip.release();
            } catch (Exception e) {
                e.printStackTrace();
                // ignore
            }
        } else {
            bufferedPlaceList.add(field);
        }
    }

    private boolean processShootList() {
        if (bufferedPlaceList.isEmpty()) {
            return false;
        }
        String[] instruction = bufferedShootList.get(0);
        bufferedShootList.remove(0);
        shoot(instruction);
        return true;
    }

    private void shoot(String[] field) {
        if (firstPlayer && masterController.getCurrentState()
                                           .equals(State.SHOOT1) || !firstPlayer && masterController
            .getCurrentState().equals(State.SHOOT2)) {
            masterController
                .shoot(Integer.parseInt(field[0]), Integer.parseInt(field[1]));
        } else {
            bufferedShootList.add(field);
        }
    }

    public void setName(String name) {
        this.masterController.setPlayerName(name);
    }

    public void startGame() {
        this.masterController.startGame();
    }

    @Override
    public void update() {
        Message msg = null;
        Logger.debug("On update getting State -> " + masterController
            .getCurrentState());
        if (firstPlayer) {
            checkFirst();
        } else {
            checkSecond();
        }
    }

    private void checkFirst() {
        Message msg = null;
        State currentState = masterController.getCurrentState();
        switch (masterController.getCurrentState()) {
            case START:
                break;

            case OPTIONS:
                break;

            case GETNAME1:
                // as this is set in the creation state this msg is not needed
                // so msg = null and nothing is sent
                break;
            case GETNAME2:
                msg = new WaitMessage();
                // if this is the second player
                // msg = null and nothing is sent
                break;

            // PLACING SHIPS
            case PLACE1:
                if (processPlaceList()) {
                    // TODO: check proper use
                    return;
                }
            case FINALPLACE1:
                this.placeOneFinished = true;
                Map<Integer, Set<Integer>> shipMap = StatCollection.createMap();
                masterController
                    .fillMap(masterController.getPlayer1().getOwnBoard()
                                             .getShipList(), shipMap, masterController
                        .getPlayer1().getOwnBoard().getShips());
                msg = new PlaceMessage(currentState, shipMap);
                break;
            case PLACE2:
            case FINALPLACE2:
                msg = new WaitMessage();
                break;
            case PLACEERR:
                if (!placeOneFinished) {
                    msg = new PlaceErrorMessage(masterController.getPlayer1()
                                                                .getOwnBoard()
                                                                .getShips() + 2);
                }
                break;

            // SHOOTING ON EACH OTHER
            case SHOOT1:
            case SHOOT2:
                // TODO: look after bufferedShootList
                // opponent = player 2
                boolean[][] shootMap = masterController.getPlayer2()
                                                       .getOwnBoard()
                                                       .getHitMap();
                boolean[][] hitMap = getHitMap(shootMap, masterController.getPlayer2());
                boolean[][] opponentShootMap = masterController.getPlayer1().getOwnBoard().getHitMap();
                msg = new ShootMessage(currentState, shootMap, hitMap, opponentShootMap);
                break;

            case HIT:
            case MISS:
                msg = new HitMessage(currentState.equals(State.HIT));
                break;

            // SOMEONE HAS WON
            case WIN1:
                Map<Integer, Set<Integer>> shipMap11 = StatCollection
                    .createMap();
                masterController
                    .fillMap(masterController.getPlayer1().getOwnBoard()
                                             .getShipList(), shipMap11, masterController
                        .getPlayer1().getOwnBoard().getShips());
                Map<Integer, Set<Integer>> shipMap21 = StatCollection
                    .createMap();
                masterController
                    .fillMap(masterController.getPlayer2().getOwnBoard()
                                             .getShipList(), shipMap21, masterController
                        .getPlayer2().getOwnBoard().getShips());
                boolean[][] winnerHitMap1 = masterController.getPlayer2()
                                                            .getOwnBoard()
                                                            .getHitMap();
                boolean[][] looserHitMap1 = masterController.getPlayer1()
                                                            .getOwnBoard()
                                                            .getHitMap();
                msg = new WinMessage(State.WIN1, masterController
                    .getPlayer1(), shipMap11, winnerHitMap1, masterController
                    .getPlayer2(), shipMap21, looserHitMap1);
                break;
            case WIN2:
                Map<Integer, Set<Integer>> shipMap12 = StatCollection
                    .createMap();
                masterController
                    .fillMap(masterController.getPlayer1().getOwnBoard()
                                             .getShipList(), shipMap12, masterController
                        .getPlayer1().getOwnBoard().getShips());
                Map<Integer, Set<Integer>> shipMap22 = StatCollection
                    .createMap();
                masterController
                    .fillMap(masterController.getPlayer2().getOwnBoard()
                                             .getShipList(), shipMap22, masterController
                        .getPlayer2().getOwnBoard().getShips());
                boolean[][] winnerHitMap2 = masterController.getPlayer1()
                                                            .getOwnBoard()
                                                            .getHitMap();
                boolean[][] looserHitMap2 = masterController.getPlayer2()
                                                            .getOwnBoard()
                                                            .getHitMap();
                msg = new WinMessage(State.WIN2, masterController
                    .getPlayer2(), shipMap22, winnerHitMap2, masterController
                    .getPlayer1(), shipMap12, looserHitMap2);
                break;
            /*
             * default case, so not needed here
             * case WRONGINPUT:
             *     break;
             */
            case END:
                break;

            default:
                msg = new InvalidMessage();
                break;
        }
        this.send(msg);
    }

    private boolean[][] getHitMap(boolean[][] shootMap, IPlayer player) {
        boolean[][] hitMap = new boolean[StatCollection.heightLenght][StatCollection.heightLenght];
        Map<Integer, Set<Integer>> shipMap = StatCollection.createMap();
        masterController
            .fillMap(player.getOwnBoard().getShipList(), shipMap, player
                .getOwnBoard().getShips());

        for (Integer y : shipMap.keySet()) {
            for (Integer x : shipMap.get(y)) {
                hitMap[x][y] = shootMap[x][y];
            }
        }
        return hitMap;
    }

    private void checkSecond() {
        Message msg = null;
        State currentState = masterController.getCurrentState();
        switch (currentState) {
            case START:
                break;

            case OPTIONS:
                break;

            case GETNAME1:
                // as this is set in the creation state this msg is not needed
                // so msg = null and nothing is sent
                break;
            case GETNAME2:
                // if this is the second player
                // msg = null and nothing is sent
                break;

            // PLACING SHIPS
            case PLACE1:
            case FINALPLACE1:
                msg = new WaitMessage();
                break;
            case PLACE2:
                if (processPlaceList()) {
                    // TODO: check proper use
                    return;
                }
            case FINALPLACE2:
                Map<Integer, Set<Integer>> shipMap = StatCollection.createMap();
                masterController
                    .fillMap(masterController.getPlayer2().getOwnBoard()
                                             .getShipList(), shipMap, masterController
                        .getPlayer2().getOwnBoard().getShips());
                msg = new PlaceMessage(currentState, shipMap);
                break;
            case PLACEERR:
                if (placeOneFinished) {
                    // minimum in PLACE2
                    msg = new PlaceErrorMessage(masterController.getPlayer2()
                                                                .getOwnBoard()
                                                                .getShips() + 2);
                }
                break;

            // SHOOTING ON EACH OTHER
            case SHOOT1:
            case SHOOT2:
                // TODO: look after bufferedShootList
                // opponent = player 1
                boolean[][] shootMap = masterController.getPlayer1().getOwnBoard()
                                                    .getHitMap();
                boolean[][] hitMap = getHitMap(shootMap, masterController.getPlayer1());
                boolean[][] opponentShootMap = masterController.getPlayer2().getOwnBoard().getHitMap();
                msg = new ShootMessage(currentState, shootMap, hitMap, opponentShootMap);
                break;
            case HIT:
                msg = new HitMessage(true);
                break;
            case MISS:
                msg = new HitMessage(false);
                break;

            // SOMEONE HAS WON
            case WIN1:
                Map<Integer, Set<Integer>> shipMap11 = StatCollection
                    .createMap();
                masterController
                    .fillMap(masterController.getPlayer1().getOwnBoard()
                                             .getShipList(), shipMap11, masterController
                        .getPlayer1().getOwnBoard().getShips());
                Map<Integer, Set<Integer>> shipMap21 = StatCollection
                    .createMap();
                masterController
                    .fillMap(masterController.getPlayer2().getOwnBoard()
                                             .getShipList(), shipMap21, masterController
                        .getPlayer2().getOwnBoard().getShips());
                boolean[][] winnerHitMap1 = masterController.getPlayer2()
                                                            .getOwnBoard()
                                                            .getHitMap();
                boolean[][] looserHitMap1 = masterController.getPlayer1()
                                                            .getOwnBoard()
                                                            .getHitMap();
                msg = new WinMessage(State.WIN1, masterController
                    .getPlayer1(), shipMap11, winnerHitMap1, masterController
                    .getPlayer2(), shipMap21, looserHitMap1);
                break;
            case WIN2:
                Map<Integer, Set<Integer>> shipMap12 = StatCollection
                    .createMap();
                masterController
                    .fillMap(masterController.getPlayer1().getOwnBoard()
                                             .getShipList(), shipMap12, masterController
                        .getPlayer1().getOwnBoard().getShips());
                Map<Integer, Set<Integer>> shipMap22 = StatCollection
                    .createMap();
                masterController
                    .fillMap(masterController.getPlayer2().getOwnBoard()
                                             .getShipList(), shipMap22, masterController
                        .getPlayer2().getOwnBoard().getShips());
                boolean[][] winnerHitMap2 = masterController.getPlayer1()
                                                            .getOwnBoard()
                                                            .getHitMap();
                boolean[][] looserHitMap2 = masterController.getPlayer2()
                                                            .getOwnBoard()
                                                            .getHitMap();
                msg = new WinMessage(State.WIN1, masterController
                    .getPlayer2(), shipMap22, winnerHitMap2, masterController
                    .getPlayer1(), shipMap12, looserHitMap2);
                break;

            /*
             * default case, so not needed here
             * case WRONGINPUT:
             *     break;
             */


            case END:
                break;

            default:
                msg = new InvalidMessage();
                break;
        }
        this.send(msg);
    }
}
