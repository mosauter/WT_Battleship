package controllers;// WuiController

import controllers.util.InvalidMessage;
import controllers.util.Message;
import controllers.util.PlaceMessage;
import controllers.util.WaitMessage;
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
                break;
            case GETNAME2:
                msg = new WaitMessage();
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
                return;
            case PLACE2:
                if (! firstPlayer) {
                    Map<Integer, Set<Integer>> shipMapPlayer2 = StatCollection.createMap();
                    masterController
                        .fillMap(masterController.getPlayer1().getOwnBoard().getShipList(), shipMapPlayer2, masterController
                            .getPlayer1().getOwnBoard().getShips());
                    msg = new PlaceMessage(State.PLACE2, shipMapPlayer2);
                    break;
                }
                return;
            case FINALPLACE2:
                if (! firstPlayer) {
                    Map<Integer, Set<Integer>> shipMapFinal2 = StatCollection.createMap();
                    masterController
                        .fillMap(masterController.getPlayer1().getOwnBoard().getShipList(), shipMapFinal2, masterController
                            .getPlayer1().getOwnBoard().getShips());
                    msg = new PlaceMessage(State.PLACE1, shipMapFinal2);
                    break;
                }
                return;
            case PLACEERR:
                msg = new InvalidMessage(State.PLACEERR);
                break;

            // SHOOTING ON EACH OTHER
            case SHOOT1:
                break;
            case SHOOT2:
                break;
            case HIT:

                break;
            case MISS:
                break;

            // SOMEONE HAS WON
            case WIN1:
                break;
            case WIN2:
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
