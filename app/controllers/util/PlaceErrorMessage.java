// PlaceErrorMessage

package controllers.util;

import de.htwg.battleship.util.State;

/**
 * PlaceErrorMessage is the message which is sent by the server if a
 * {@link State#PLACEERR} raises.
 *
 * The Message in JSON via {@link Message#toJSON()} should result in:
 * <pre>
 *     {@code
 *     {
 *         "type": PLACEERR,
 *         "errShipLength": int
 *     }
 *     }
 * </pre>
 * @author ms
 * @since 2016-01-11
 */
public class PlaceErrorMessage extends Message {
    /**
     * This int specifies which ship, identified by the ship length caused the
     * {@link State#PLACEERR}.
     */
    private final int errShipLength;

    public PlaceErrorMessage(int shipLength) {
        this.type = State.PLACEERR;
        this.errShipLength = shipLength;
    }
}
