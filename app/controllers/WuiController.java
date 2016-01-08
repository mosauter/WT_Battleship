package controllers;// WuiController

import controllers.util.HitMessage;
import controllers.util.InvalidMessage;
import controllers.util.Message;
import controllers.util.PlaceMessage;
import controllers.util.ShootMessage;
import controllers.util.WaitMessage;
import controllers.util.WinMessage;
import de.htwg.battleship.controller.IMasterController;
import de.htwg.battleship.observer.IObserver;
import de.htwg.battleship.util.StatCollection;
import de.htwg.battleship.util.State;
import play.mvc.WebSocket;

import java.util.Map;
import java.util.Set;

/**
 * WuiController
 *
 * @author ms
 * @since 2015-11-25
 */
public class WuiController implements IObserver {

    private final IMasterController masterController;
    private final WebSocket.Out<String> socket;
    private final boolean firstPlayer;

    public WuiController(IMasterController masterController, WebSocket.Out<String> socket, boolean first) {
        this.masterController = masterController;
        this.socket = socket;
        this.firstPlayer = first;
        masterController.addObserver(this);
    }

    private void send(Message msg) {
        socket.write(msg.toJSON());
    }

    public void analyzeMessage(String message) {

    }

    public void setName(String name) {
        this.masterController.setPlayerName(name);
    }

    public void startGame() {
        this.masterController.startGame();
    }

    @Override
    public void update() {
        Message msg = new InvalidMessage(State.WRONGINPUT);
        switch (masterController.getCurrentState()) {
            case START:
                break;

            case OPTIONS:
                break;

            case GETNAME1:
                // as this is set in the creation state this msg is not needed
                break;
            case GETNAME2:
                if (firstPlayer) {
                    msg = new WaitMessage();
                }
                break;

            // PLACING SHIPS
            case PLACE1:
                if (firstPlayer) {
                    Map<Integer, Set<Integer>> shipMapPlayer1 = StatCollection.createMap();
                    masterController
                        .fillMap(masterController.getPlayer1().getOwnBoard().getShipList(), shipMapPlayer1, masterController
                            .getPlayer1().getOwnBoard().getShips());
                    msg = new PlaceMessage(State.PLACE1, shipMapPlayer1);
                    break;
                }
                // if secondPlayer he should not get a message
                return;
            case FINALPLACE1:
                if (firstPlayer) {
                    Map<Integer, Set<Integer>> shipMapFinal1 = StatCollection.createMap();
                    masterController
                        .fillMap(masterController.getPlayer1().getOwnBoard().getShipList(), shipMapFinal1, masterController
                            .getPlayer1().getOwnBoard().getShips());
                    msg = new PlaceMessage(State.PLACE1, shipMapFinal1);
                    break;
                }
                // if secondPlayer he should not get a message
                return;
            case PLACE2:
                if (! firstPlayer) {
                    Map<Integer, Set<Integer>> shipMapPlayer2 = StatCollection.createMap();
                    masterController
                        .fillMap(masterController.getPlayer2().getOwnBoard().getShipList(), shipMapPlayer2, masterController
                            .getPlayer2().getOwnBoard().getShips());
                    msg = new PlaceMessage(State.PLACE2, shipMapPlayer2);
                    break;
                }
                // if firstPlayer he should not get a message
                return;
            case FINALPLACE2:
                if (! firstPlayer) {
                    Map<Integer, Set<Integer>> shipMapFinal2 = StatCollection.createMap();
                    masterController
                        .fillMap(masterController.getPlayer2().getOwnBoard().getShipList(), shipMapFinal2, masterController
                            .getPlayer2().getOwnBoard().getShips());
                    msg = new PlaceMessage(State.PLACE1, shipMapFinal2);
                    break;
                }
                // if firstPlayer he should not get a message
                return;
            case PLACEERR:
                msg = new InvalidMessage(State.PLACEERR);
                break;

            // SHOOTING ON EACH OTHER
            case SHOOT1:
                boolean[][] field1;
                if (firstPlayer) {
                    // opponent = player 2
                    field1 = masterController.getPlayer2().getOwnBoard().getHitMap();
                } else {
                    // opponent = player 1
                    field1 = masterController.getPlayer1().getOwnBoard().getHitMap();
                }
                msg = new ShootMessage(State.SHOOT1, field1);
                break;
            case SHOOT2:
                boolean[][] field2;
                if (firstPlayer) {
                    // opponent = player 2
                    field2 = masterController.getPlayer2().getOwnBoard().getHitMap();
                } else {
                    // opponent = player 1
                    field2 = masterController.getPlayer1().getOwnBoard().getHitMap();
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
                Map<Integer, Set<Integer>> shipMap11 = StatCollection.createMap();
                masterController
                    .fillMap(masterController.getPlayer1().getOwnBoard().getShipList(), shipMap11, masterController
                        .getPlayer1().getOwnBoard().getShips());
                Map<Integer, Set<Integer>> shipMap21 = StatCollection.createMap();
                masterController
                    .fillMap(masterController.getPlayer2().getOwnBoard().getShipList(), shipMap21, masterController
                        .getPlayer2().getOwnBoard().getShips());
                boolean[][] winnerHitMap1 = masterController.getPlayer2().getOwnBoard().getHitMap();
                boolean[][] looserHitMap1 = masterController.getPlayer1().getOwnBoard().getHitMap();
                msg = new WinMessage(State.WIN1, masterController.getPlayer1(), shipMap11, winnerHitMap1, masterController.getPlayer2(), shipMap21, looserHitMap1);
                break;
            case WIN2:
                Map<Integer, Set<Integer>> shipMap12 = StatCollection.createMap();
                masterController
                    .fillMap(masterController.getPlayer1().getOwnBoard().getShipList(), shipMap12, masterController
                        .getPlayer1().getOwnBoard().getShips());
                Map<Integer, Set<Integer>> shipMap22 = StatCollection.createMap();
                masterController
                    .fillMap(masterController.getPlayer2().getOwnBoard().getShipList(), shipMap22, masterController
                        .getPlayer2().getOwnBoard().getShips());
                boolean[][] winnerHitMap2 = masterController.getPlayer1().getOwnBoard().getHitMap();
                boolean[][] looserHitMap2 = masterController.getPlayer2().getOwnBoard().getHitMap();
                msg = new WinMessage(State.WIN1, masterController.getPlayer2(), shipMap22, winnerHitMap2, masterController.getPlayer1(), shipMap12, looserHitMap2);
                break;

            /*
             * default case, so not needed here
             * case WRONGINPUT:
             *     break;
             */



            case END:
                break;

            default:
                msg = new InvalidMessage(State.WRONGINPUT);
                break;
        }
        socket.write(msg.toJSON());
    }
}
