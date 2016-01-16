// WinMessage

package controllers.util;

import de.htwg.battleship.model.IPlayer;
import de.htwg.battleship.util.State;

import java.util.Map;
import java.util.Set;

/**
 * The WinMessage is the message which is sent by the server to a client
 * if the game is in the {@link de.htwg.battleship.util.State#WIN1} or
 * in the {@link de.htwg.battleship.util.State#WIN2} state.
 *
 * The Message in JSON via {@link Message#toJSON()} should result in:
 * <pre>
 *     {@code
 *     {
 *         "type": ( WIN1 | WIN2 ),
 *         "winner": {},
 *         "winnerMap: {},
 *         "winnerShootMap: [[]],
 *         "looser": {},
 *         "looserMap": {},
 *         "looserShootMap": [[]]
 *     }
 *     }
 * </pre>
 *
 * @author Moritz Sauter
 * @since 2015-11-25
 */
public class WinMessage extends Message {

    private final IPlayer winner;
    private final Map<Integer, Set<Integer>> winnerMap;
    private final boolean[][] winnerShootMap;

    private final IPlayer looser;
    private final Map<Integer, Set<Integer>> looserMap;
    private final boolean[][] looserShootMap;

    public WinMessage(State type, IPlayer winner,
                      Map<Integer, Set<Integer>> winnerMap,
                      boolean[][] winnerShootMap, IPlayer looser,
                      Map<Integer, Set<Integer>> looserMap,
                      boolean[][] looserShootMap) {
        this.type = type.toString();
        this.winner = winner;
        this.winnerMap = winnerMap;
        this.winnerShootMap = winnerShootMap;
        this.looser = looser;
        this.looserMap = looserMap;
        this.looserShootMap = looserShootMap;
    }
}
