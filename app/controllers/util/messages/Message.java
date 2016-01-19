package controllers.util.messages;

import com.google.gson.Gson;

/**
 * Message
 *
 * @author ms
 * @since 2015-11-25
 */
public abstract class Message {

    protected String type;

    public String toJSON() {
        return new Gson().toJson(this);
    }
}
