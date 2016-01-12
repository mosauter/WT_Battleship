// FirstPlayerMessage

package controllers.util;

import de.htwg.battleship.util.State;

/**
 * FirstPlayerMessage
 *
 * @author ms
 * @since 2016-01-11
 */
public class FirstPlayerMessage extends Message {

    private final boolean firstPlayer;

    public FirstPlayerMessage(boolean firstPlayer) {
        this.type = State.START.toString();
        this.firstPlayer = firstPlayer;
    }
}
