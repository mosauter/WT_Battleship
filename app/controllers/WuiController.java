package controllers;// WuiController

import controllers.util.*;
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
    private boolean placeOneFinished = false;
    private final AliveSender aliveSender;

    private final Semaphore addShip;

    public WuiController(IMasterController masterController,
                         WebSocket.Out<String> socket, boolean first) {
        this.masterController = masterController;
        this.socket = socket;
        this.firstPlayer = first;
        this.bufferedPlaceList = new LinkedList<>();
        masterController.addObserver(this);
        this.send(new FirstPlayerMessage(first));
        this.addShip = new Semaphore(1);
        this.aliveSender = new AliveSender(socket);
    }

    @Override
    public void update() {
        Logger.debug(
            "On update getting State -> " + masterController.getCurrentState());
        if (firstPlayer) {
            checkFirst();
        } else {
            checkSecond();
        }
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
            // TODO: evaluate maybe "getEntireUpdate"
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
        if (firstPlayer && masterController.getCurrentState() == State.PLACE1 ||
            !firstPlayer && masterController.getCurrentState() == State.PLACE2) {
            try {
                addShip.acquire();
                masterController.placeShip(Integer.parseInt(field[0]),
                                           Integer.parseInt(field[1]), field[2]
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

    private void shoot(String[] field) {
        if (firstPlayer && masterController.getCurrentState() == State.SHOOT1 ||
            !firstPlayer && masterController.getCurrentState() == State.SHOOT2) {
            masterController
                .shoot(Integer.parseInt(field[0]), Integer.parseInt(field[1]));
        }
        // else -> the player isn't allowed to shoot in this state
        // instruction is omitted
    }

    public void setName(String name) {
        this.masterController.setPlayerName(name);
    }

    public void startGame() {
        this.masterController.startGame();
    }

    private boolean[][] getShootMap(boolean[][] shootMap, IPlayer player) {
        boolean[][] hitMap =
            new boolean[StatCollection.heightLenght][StatCollection.heightLenght];
        Map<Integer, Set<Integer>> shipMap = getShipMap(player);
        for (Integer y : shipMap.keySet()) {
            for (Integer x : shipMap.get(y)) {
                hitMap[x][y] = shootMap[x][y];
            }
        }
        return hitMap;
    }

    private WinMessage createWinMessage(State state) {
        IPlayer winner = state == State.WIN1 ? masterController
            .getPlayer1() : masterController.getPlayer2();
        IPlayer looser = state == State.WIN1 ? masterController
            .getPlayer2() : masterController.getPlayer1();
        Map<Integer, Set<Integer>> winnerShips = getShipMap(winner);
        Map<Integer, Set<Integer>> looserShips = getShipMap(looser);

        // shoot map -> winner shoots at looser
        boolean[][] looserShootMap = winner.getOwnBoard().getHitMap();
        boolean[][] winnerShootMap = looser.getOwnBoard().getHitMap();

        return new WinMessage(state, winner, winnerShips, winnerShootMap,
                              looser, looserShips, looserShootMap);
    }

    private ShootMessage createShootMessage(State state, IPlayer self,
                                            IPlayer opponent) {
        boolean[][] shootMap = opponent.getOwnBoard().getHitMap();
        boolean[][] hitMap = getShootMap(shootMap, opponent);
        boolean[][] opponentShootMap = self.getOwnBoard().getHitMap();
        return new ShootMessage(state, shootMap, hitMap, opponentShootMap);
    }

    private Map<Integer, Set<Integer>> getShipMap(IPlayer player) {
        Map<Integer, Set<Integer>> shipMap = StatCollection.createMap();
        masterController.fillMap(player.getOwnBoard().getShipList(), shipMap,
                                 player.getOwnBoard().getShips());
        return shipMap;
    }

    private void checkFirst() {
        Message msg = null;
        State currentState = masterController.getCurrentState();
        switch (masterController.getCurrentState()) {
            case GETNAME2:
                msg = createWaitMessage();
                // if this is the second player
                // msg = null and nothing is sent
                break;

            // PLACING SHIPS
            case PLACE1:
                if (processPlaceList()) {
                    return;
                }
            case FINALPLACE1:
                this.placeOneFinished = true;
                Map<Integer, Set<Integer>> shipMap =
                    getShipMap(masterController.getPlayer1());
                msg = new PlaceMessage(currentState, shipMap);
                break;
            case PLACE2:
            case FINALPLACE2:
                msg = createWaitMessage();
                break;
            case PLACEERR:
                if (!placeOneFinished) {
                    msg = new PlaceErrorMessage(
                        masterController.getPlayer1().getOwnBoard().getShips() +
                        2);
                }
                break;

            // SHOOTING ON EACH OTHER
            case SHOOT1:
            case SHOOT2:
                // opponent = player 2
                msg = createShootMessage(currentState,
                                         masterController.getPlayer1(),
                                         masterController.getPlayer2());
                break;

            case HIT:
            case MISS:
                msg = new HitMessage(currentState == State.HIT);
                break;

            // SOMEONE HAS WON
            case WIN1:
            case WIN2:
                msg = createWinMessage(currentState);
                break;

            // SOME INVALID INPUT
            case WRONGINPUT:
                msg = new InvalidMessage();
                break;

            default:
                break;
        }
        this.send(msg);
    }

    private void checkSecond() {
        Message msg = null;
        State currentState = masterController.getCurrentState();
        switch (currentState) {
            // PLACING SHIPS
            case PLACE1:
            case FINALPLACE1:
                msg = createWaitMessage();
                break;
            case PLACE2:
                if (processPlaceList()) {
                    return;
                }
            case FINALPLACE2:
                Map<Integer, Set<Integer>> shipMap =
                    getShipMap(masterController.getPlayer2());
                msg = new PlaceMessage(currentState, shipMap);
                break;
            case PLACEERR:
                if (placeOneFinished) {
                    // minimum in PLACE2
                    msg = new PlaceErrorMessage(
                        masterController.getPlayer2().getOwnBoard().getShips() +
                        2);
                }
                break;

            // SHOOTING ON EACH OTHER
            case SHOOT1:
            case SHOOT2:
                // opponent = player 1
                msg = createShootMessage(currentState,
                                         masterController.getPlayer2(),
                                         masterController.getPlayer1());
                break;
            case HIT:
                msg = new HitMessage(true);
                break;
            case MISS:
                msg = new HitMessage(false);
                break;

            // SOMEONE HAS WON
            case WIN1:
            case WIN2:
                msg = createWinMessage(currentState);
                break;

            // SOME INVALID INPUT
            case WRONGINPUT:
                msg = new InvalidMessage();
                break;

            default:
                break;
        }
        this.send(msg);
    }

    private WaitMessage createWaitMessage() {
        if (firstPlayer) {
            return new WaitMessage(masterController.getPlayer1().getName(),
                                   masterController.getPlayer2().getName());
        }
        return new WaitMessage(masterController.getPlayer2().getName(),
                               masterController.getPlayer1().getName());
    }

    public void setAliveDone() {
        aliveSender.setDone();
    }

    public void closedSocket() {
        this.aliveSender.setDone();
        // sending Winn message -> other player left game / killed his socket
        this.send(createWinMessage(firstPlayer ? State.WIN1 : State.WIN2));
    }
}
