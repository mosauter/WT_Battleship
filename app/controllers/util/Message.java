package controllers.util;

import com.google.gson.Gson;

import de.htwg.battleship.util.State;

/**
 * Message
 *
 * @author ms
 * @since 2015-11-25
 */
public abstract class Message {

    protected State type;

    public String toJSON() {
        return new Gson().toJson(this);
    }
}
