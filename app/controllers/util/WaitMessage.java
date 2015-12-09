// WaitMessage

package controllers.util;

import de.htwg.battleship.util.State;

/**
 * WaitMessage signals the client that the he must wait for a second player to
 * join the current lobby.
 *
 * As mask State for this we use {@link de.htwg.battleship.util.State#GETNAME2}.
 * So the structure is:
 *
 * <pre>
 *     {
 *         "type": "GETNAME2"
 *     }
 * </pre>
 *
 * @author ms
 * @since 2015-12-02
 */
public class WaitMessage extends Message {

    public WaitMessage() {
        this.type = State.GETNAME2;
    }
}
