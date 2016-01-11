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

    private static Semaphore addShip;

    public WuiController(IMasterController masterController, WebSocket.Out<String> socket, boolean first) {
        this.masterController = masterController;
        this.socket = socket;
        this.firstPlayer = first;
        this.bufferedPlaceList = new LinkedList<>();
        this.bufferedShootList = new LinkedList<>();
        masterController.addObserver(this);
        this.send(new FirstPlayerMessage(first));
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
        // x y orientation -> which player
        if (field.length == 3) {
            placeShip(field);
        }
        // x y -> test which player
        if (field.length == 2) {
            shoot(field);
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
        if (firstPlayer && masterController.getCurrentState()
                                           .equals(State.PLACE1)
            || !firstPlayer && masterController.getCurrentState()
                                               .equals(State.PLACE2)) {
            try {
                addShip.acquire();
                System.out.println("adding ship -> " + (firstPlayer && masterController
                    .getCurrentState()
                    .equals(State.PLACE1) || !firstPlayer && masterController.getCurrentState().equals(State.PLACE2)));

                masterController.placeShip(Integer.parseInt(field[0]), Integer.parseInt(field[1]), field[2]
                    .equals(HORIZONTAL_ORIENTATION));
                System.out.println("HALLO DU I");
                addShip.release();
            } catch (Exception e) {
                System.out.println(e);
                // ignore
            }
        } else {
            bufferedPlaceList.add(field);
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
        State currentState = masterController.getCurrentState();
        Logger.debug("On update getting State -> " + currentState);
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
                if (firstPlayer) {
                    msg = new WaitMessage();
                }
                // if this is the second player
                // msg = null and nothing is sent
                break;

            // PLACING SHIPS
            case PLACE1:
                if (firstPlayer) {
                    if (processPlaceList()) {
                        // TODO: check proper use
                        return;
                    }
                    Map<Integer, Set<Integer>> shipMapPlayer1 = StatCollection
                        .createMap();
                    masterController
                        .fillMap(masterController.getPlayer1().getOwnBoard()
                                                 .getShipList(), shipMapPlayer1, masterController
                            .getPlayer1().getOwnBoard().getShips());
                    msg = new PlaceMessage(State.PLACE1, shipMapPlayer1);
                    break;
                }
                // if secondPlayer he should not get a message
                return;
            case FINALPLACE1:
                this.placeOneFinished = true;
                if (firstPlayer) {
                    Map<Integer, Set<Integer>> shipMapFinal1 = StatCollection
                        .createMap();
                    masterController
                        .fillMap(masterController.getPlayer1().getOwnBoard()
                                                 .getShipList(), shipMapFinal1, masterController
                            .getPlayer1().getOwnBoard().getShips());
                    msg = new PlaceMessage(State.PLACE1, shipMapFinal1);
                    break;
                }
                // if secondPlayer he should not get a message
                return;
            case PLACE2:
                if (!firstPlayer) {
                    if (processPlaceList()) {
                        // TODO: check proper use
                        return;
                    }
                    Map<Integer, Set<Integer>> shipMapPlayer2 = StatCollection
                        .createMap();
                    masterController
                        .fillMap(masterController.getPlayer2().getOwnBoard()
                                                 .getShipList(), shipMapPlayer2, masterController
                            .getPlayer2().getOwnBoard().getShips());
                    msg = new PlaceMessage(State.PLACE2, shipMapPlayer2);
                    break;
                }
                // if firstPlayer he should not get a message
                return;
            case FINALPLACE2:
                if (!firstPlayer) {
                    Map<Integer, Set<Integer>> shipMapFinal2 = StatCollection
                        .createMap();
                    masterController
                        .fillMap(masterController.getPlayer2().getOwnBoard()
                                                 .getShipList(), shipMapFinal2, masterController
                            .getPlayer2().getOwnBoard().getShips());
                    msg = new PlaceMessage(State.PLACE1, shipMapFinal2);
                    break;
                }
                // if firstPlayer he should not get a message
                return;
            case PLACEERR:
                if (placeOneFinished) {
                    // minimum in PLACE2
                    msg = new PlaceErrorMessage(masterController.getPlayer2()
                                                                .getOwnBoard()
                                                                .getShips() + 2);
                } else {
                    msg = new PlaceErrorMessage(masterController.getPlayer1()
                                                                .getOwnBoard()
                                                                .getShips() + 2);
                }
                break;

            // SHOOTING ON EACH OTHER
            case SHOOT1:
                // TODO: look after bufferedShootList
                boolean[][] field1;
                if (firstPlayer) {
                    // opponent = player 2
                    field1 = masterController.getPlayer2().getOwnBoard()
                                             .getHitMap();
                } else {
                    // opponent = player 1
                    field1 = masterController.getPlayer1().getOwnBoard()
                                             .getHitMap();
                }
                msg = new ShootMessage(State.SHOOT1, field1);
                break;
            case SHOOT2:
                // TODO: look after bufferedShootList
                boolean[][] field2;
                if (firstPlayer) {
                    // opponent = player 2
                    field2 = masterController.getPlayer2().getOwnBoard()
                                             .getHitMap();
                } else {
                    // opponent = player 1
                    field2 = masterController.getPlayer1().getOwnBoard()
                                             .getHitMap();
                }
                msg = new ShootMessage(State.SHOOT1, field2);
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
