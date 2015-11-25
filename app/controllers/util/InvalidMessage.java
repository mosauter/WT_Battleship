// InvalidMessage

package controllers.util;

import de.htwg.battleship.util.State;

/**
 * The InvalidMessage is sent in the
 * {@link de.htwg.battleship.util.State#WRONGINPUT} and in the
 * {@link de.htwg.battleship.util.State#PLACEERR} state.
 *
 * The Message in JSON via {@link Message#toJSON()} should result in:
 * <pre>
 *     {@code
 *     {
 *         "type": ( WRONGINPUT | PLACEERR )
 *     }
 *     }
 * </pre>
 *
 * @author ms
 * @since 2015-11-25
 */
public class InvalidMessage extends Message {

    public InvalidMessage(State type) {
        this.type = type;
    }

}
