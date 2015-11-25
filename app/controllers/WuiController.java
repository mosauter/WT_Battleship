package controllers;// WuiController

import de.htwg.battleship.controller.IMasterController;
import de.htwg.battleship.observer.IObserver;

/**
 * WuiController
 *
 * @author ms
 * @since 2015-11-25
 */
public class WuiController implements IObserver {

    private IMasterController masterController;

    public WuiController(IMasterController masterController) {
        this.masterController = masterController;
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
