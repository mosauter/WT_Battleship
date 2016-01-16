// AliveMessage

package controllers.util;

/**
 * AliveMessage is very useless message. It's only necessary for Heroku.
 *
 * As Heroku breaks each Communication, also sockets, after 55 seconds.
 * So this should send a AliveMessage after 30 seconds.
 *
 * @author ms
 * @since 2016-01-14
 */
public class AliveMessage extends Message {

    public AliveMessage() {
        this.type = "ALIVE MESSAGE";
    }
}
