// GameInstance

package controllers.util;

import controllers.WuiController;
import controllers.util.messages.ChatMessage;
import de.htwg.battleship.Battleship;
import play.mvc.WebSocket.Out;

import java.util.UUID;

/**
 * GameInstance is a wrapper for a single instance of a {@link de.htwg.battleship.Battleship} game. So for that it
 * contains both sockets to communicate with the players.
 * <p>
 * To initialize an instance you need the first socket for this player. Via a setter you can later add a second socket.
 *
 * @author ms
 * @since 2015-12-05
 */
public class GameInstance {
    public static final String CHAT_PREFIX = "CHAT ";
    /**
     * Unique ID of each GameInstance.
     */
    private final UUID uuid;
    /**
     * Single instance of the game -> {@link de.htwg.battleship.Battleship}
     */
    private final Battleship instance;
    /**
     * The {@link WuiController} for the first player.
     */
    private final WuiController wuiControllerOne;
    /**
     * The {@link play.mvc.WebSocket.Out} socket of the first player.
     */
    private final Out socketOne;
    /**
     * The {@link WuiController} for the second player.
     */
    private WuiController wuiControllerTwo;
    /**
     * The {@link play.mvc.WebSocket.Out} socket of the second player.
     */
    private Out socketTwo;
    /**
     * This boolean indicates if one of the two players has closed his socket. So this keeps the sockets from sending
     * again and preventing {@link java.nio.channels.ClosedChannelException}
     */
    private boolean closedSockets;

    public GameInstance(final Battleship instance, final Out socketOne, final WuiController wuiControllerOne) {
        this.uuid = UUID.randomUUID();
        this.instance = instance;
        this.wuiControllerOne = wuiControllerOne;
        this.socketOne = socketOne;
        this.closedSockets = false;
        this.wuiControllerOne.setGameInstance(this);
    }

    public Battleship getInstance() {
        return instance;
    }

    public Out getSocketOne() {
        return socketOne;
    }

    public Out getSocketTwo() {
        return socketTwo;
    }

    public void setSocketTwo(Out socketTwo) {
        this.socketTwo = socketTwo;
    }

    public WuiController getWuiControllerOne() {
        return wuiControllerOne;
    }

    public WuiController getWuiControllerTwo() {
        return wuiControllerTwo;
    }

    public void setWuiControllerTwo(final WuiController wuiControllerTwo) {
        this.wuiControllerTwo = wuiControllerTwo;
        this.wuiControllerTwo.setGameInstance(this);
    }

    /**
     * Is called when one player closes his socket. So this ends the 'Heart Beater' {@link AliveSender} and delegates
     * the event to the {@link WuiController}.
     *
     * @param playerOne indicates which player closed his socket and caused this event true    indicates that this was
     *                  the first player false   indicates that this was the second player
     */
    public void closedSocket(boolean playerOne) {
        if (closedSockets) {
            return;
        }
        closedSockets = true;
        if (playerOne && wuiControllerTwo != null) {
            // player one closed the socket
            this.wuiControllerOne.setAliveDone();
            this.wuiControllerOne.closeSocket();
            this.wuiControllerTwo.closedSocket();
        } else {
            // player two closed the socket
            if (this.wuiControllerTwo != null) {
                this.wuiControllerTwo.setAliveDone();
                this.wuiControllerTwo.closeSocket();
            }
            this.wuiControllerOne.closedSocket();
        }
    }

    /**
     * This is a method which should enable the two {@link WuiController} to chat with each other. It will delegate a
     * {@link ChatMessage} to the {@link WuiController#chat(ChatMessage)} of the both players. So the players receive
     * also their own messages again.
     *
     * @param message     the chat message of the player to the other in the format: "CHAT [/w]"
     * @param firstPlayer true if the origin of the ChatMessage was the first player, false if the second player was the
     *                    origin
     */
    public void chat(String message, boolean firstPlayer) {
        String msg = message.replace(GameInstance.CHAT_PREFIX, "");
        ChatMessage msgObject;
        if (firstPlayer) {
            // send by Player one -> name of IPlayer1
            msgObject = new ChatMessage(msg, this.instance.getMasterController().getPlayer1().getName());
        } else {
            // send by Player two -> name of IPlayer2
            msgObject = new ChatMessage(msg, this.instance.getMasterController().getPlayer2().getName());
        }
        this.getWuiControllerOne().chat(msgObject);
        this.getWuiControllerTwo().chat(msgObject);
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
