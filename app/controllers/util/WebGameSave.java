// WebGameSave

package controllers.util;

import de.htwg.battleship.util.GameMode;
import de.htwg.battleship.util.State;

import java.util.Map;
import java.util.Set;

/**
 * WebGameSave
 *
 * @author ms
 * @since 2016-06-20
 */
public class WebGameSave {
    private final String player1Name;
    private final String player1ID;
    private final String player2Name;
    private final String player2ID;
    private final GameMode gameMode;
    private final State currentState;
    private final boolean[][] field1;
    private final boolean[][] field2;
    private final Map<Integer, Set<Integer>> shipList1;
    private final Map<Integer, Set<Integer>> shipList2;
    private final int heightLength;
    private final int maxShipNumber;

    public WebGameSave(String player1Name, String player1ID, String player2Name, String player2ID, GameMode gameMode,
                       State currentState, boolean[][] field1, boolean[][] field2, Map<Integer, Set<Integer>> shipList1,
                       Map<Integer, Set<Integer>> shipList2, int heightLength, int maxShipNumber) {
        this.player1Name = player1Name;
        this.player1ID = player1ID;
        this.player2Name = player2Name;
        this.player2ID = player2ID;
        this.gameMode = gameMode;
        this.currentState = currentState;
        this.field1 = field1;
        this.field2 = field2;
        this.shipList1 = shipList1;
        this.shipList2 = shipList2;
        this.heightLength = heightLength;
        this.maxShipNumber = maxShipNumber;
    }
}
