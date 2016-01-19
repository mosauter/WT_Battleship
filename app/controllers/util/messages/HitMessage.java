// HitMessage

package controllers.util.messages;

import de.htwg.battleship.util.State;

/**
 * The HitMessage is the message which is sent in the
 * {@link de.htwg.battleship.util.State#HIT} or
 * {@link de.htwg.battleship.util.State#MISS} state.
 *
 * The Message in JSON via {@link Message#toJSON()} should result in:
 * <pre>
 *     {@code
 *     {
 *         "type": ( HIT | MISS ),
 *         "wasHit": ( true | false )
 *     }
 *     }
 * </pre>
 *
 * @author ms
 * @since 2015-11-25
 */
public class HitMessage extends Message {

    private final boolean wasHit;

    public HitMessage(boolean wasHit) {
        this.wasHit = wasHit;
        this.type = wasHit ? State.HIT.toString() : State.MISS.toString();
    }
}
