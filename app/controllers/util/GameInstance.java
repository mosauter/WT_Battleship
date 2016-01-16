// GameInstance

package controllers.util;

import controllers.WuiController;
import de.htwg.battleship.Battleship;
import play.mvc.WebSocket.Out;

import java.util.UUID;

/**
 * GameInstance is a wrapper for a single instance of a {@link
 * de.htwg.battleship.Battleship} game. So for that it contains both sockets to
 * communicate with the players.
 * <p>
 * To initialize an instance you need the first socket for this player. Via a
 * setter you can later add a second socket.
 *
 * @author ms
 * @since 2015-12-05
 */
public class GameInstance {
    /**
     * Unique ID of each GameInstance.
     */
    private final UUID uuid;
    private final Battleship instance;
    private final WuiController wuiControllerOne;
    private final Out socketOne;
    private WuiController wuiControllerTwo;
    private Out socketTwo;
    private boolean closedSockets;

    public GameInstance(final Battleship instance, final Out socketOne,
                        final WuiController wuiControllerOne) {
        this.uuid = UUID.randomUUID();
        this.instance = instance;
        this.wuiControllerOne = wuiControllerOne;
        this.socketOne = socketOne;
        closedSockets = false;
    }

    public void setWuiControllerTwo(WuiController wuiControllerTwo) {
        this.wuiControllerTwo = wuiControllerTwo;
    }

    public WuiController getWuiControllerOne() {

        return wuiControllerOne;
    }

    public WuiController getWuiControllerTwo() {
        return wuiControllerTwo;
    }

    public Battleship getInstance() {
        return instance;
    }

    public Out getSocketOne() {
        return socketOne;
    }

    public void setSocketTwo(Out socketTwo) {
        this.socketTwo = socketTwo;
    }

    public Out getSocketTwo() {
        return socketTwo;
    }

    public void closedSocket(boolean playerOne) {
        if (closedSockets) {
            return;
        }
        closedSockets = true;
        if (playerOne && wuiControllerTwo != null) {
            // player one closed the socket
            this.wuiControllerOne.setAliveDone();
            this.wuiControllerTwo.closedSocket();
        } else {
            // player two closed the socket
            this.wuiControllerTwo.setAliveDone();
            this.wuiControllerOne.closedSocket();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GameInstance)) return false;

        GameInstance that = (GameInstance) o;

        return uuid.equals(that.uuid);
    }

    @Override
    public int hashCode() {
        return uuid.hashCode();
    }
}
