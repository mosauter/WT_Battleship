// WaitMessage

package controllers.util.messages;

/**
 * WaitMessage signals the client that the he must wait for a second player to
 * join the current lobby.
 *
 * As mask State for this we use {@link de.htwg.battleship.util.State#GETNAME2}.
 * So the structure is:
 *
 * <pre>
 *     {
 *         "type": "WAIT",
 *         "yourName": "Name",
 *         "opponentName": "Name"
 *     }
 * </pre>
 *
 * @author ms
 * @since 2015-12-02
 */
public class WaitMessage extends Message {

    private final String yourName;
    private final String opponentName;

    public WaitMessage(String yourName, String opponentName) {
        this.yourName = yourName;
        this.opponentName = opponentName;
        this.type = "WAIT";
    }

}
