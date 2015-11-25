// ShootMessage

package controllers.util;

import de.htwg.battleship.util.State;

/**
 * The ShootMessage is sent in the {@link de.htwg.battleship.util.State#SHOOT1}
 * or in the {@link de.htwg.battleship.util.State#SHOOT2} state.
 *
 * The Message in JSON via {@link Message#toJSON()} should result in:
 * <pre>
 *     {@code
 *     {
 *         "type": ( SHOOT1 | SHOOT2 ),
 *         "isHitMap": [[]]
 *     }
 *     }
 * </pre>
 *
 * @author ms
 * @since 2015-11-25
 */
public class ShootMessage extends Message {

    private final boolean[][] isHitMap;

    public ShootMessage(State type, boolean[][] isHitMap) {
        this.type = type;
        this.isHitMap = isHitMap;
    }
}
