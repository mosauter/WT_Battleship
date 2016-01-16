// InvalidMessage

package controllers.util;

import de.htwg.battleship.util.State;

/**
 * The InvalidMessage is sent in the
 * {@link de.htwg.battleship.util.State#WRONGINPUT} state.
 *
 * The Message in JSON via {@link Message#toJSON()} should result in:
 * <pre>
 *     {@code
 *     {
 *         "type": WRONGINPUT
 *     }
 *     }
 * </pre>
 *
 * @author ms
 * @since 2015-11-25
 */
public class InvalidMessage extends Message {

    public InvalidMessage() {
        this.type = State.WRONGINPUT.toString();
    }

}
