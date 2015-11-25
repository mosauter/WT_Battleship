// PlaceMessage

package controllers.util;

import de.htwg.battleship.util.State;

import java.util.Map;
import java.util.Set;

/**
 * The PlaceMessage is the message which is sent in the
 * {@link de.htwg.battleship.util.State#PLACE1},
 * the {@link de.htwg.battleship.util.State#PLACE2},
 * the {@link de.htwg.battleship.util.State#FINALPLACE1} and
 * the {@link de.htwg.battleship.util.State#FINALPLACE2} state.
 *
 * The Message in JSON via {@link Message#toJSON()} should result in:
 * <pre>
 *     {@code
 *     {
 *         "type": ( PLACE1 | PLACE2 | FINALPLACE1 | FINALPLACE2 ),
 *         "shipMap": {}
 *     }
 *     }
 * </pre>
 *
 * @author ms
 * @since 2015-11-25
 */
public class PlaceMessage extends Message {

    private final Map<Integer, Set<Integer>> shipMap;

    public PlaceMessage(State type, Map<Integer, Set<Integer>> shipMap) {
        this.type = type;
        this.shipMap = shipMap;
    }
}
