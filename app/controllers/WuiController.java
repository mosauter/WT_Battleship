package controllers;// WuiController

import controllers.util.Message;
import de.htwg.battleship.controller.IMasterController;
import de.htwg.battleship.observer.IObserver;
import play.mvc.WebSocket;

/**
 * WuiController
 *
 * @author ms
 * @since 2015-11-25
 */
public class WuiController implements IObserver {

    private final IMasterController masterController;
    private final WebSocket.Out<String> socket;

    public WuiController(IMasterController masterController, WebSocket.Out<String> socket) {
        this.masterController = masterController;
        this.socket = socket;
        masterController.addObserver(this);
    }

    private void send(Message msg) {
        socket.write(msg.toJSON());
    }

    @Override
    public void update() {
        switch (masterController.getCurrentState()) {
            case START:
                break;

            case OPTIONS:
                break;

            case GETNAME1:
                break;
            case GETNAME2:
                break;

            // PLACING SHIPS
            case PLACE1:
                break;
            case FINALPLACE1:
                break;
            case PLACE2:
                break;
            case FINALPLACE2:
                break;

            case PLACEERR:
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

            case WRONGINPUT:
                break;

            case END:
                break;

            default:
                break;
        }
    }
}
