// AliveSender

package controllers.util;

import controllers.util.messages.AliveMessage;
import play.mvc.WebSocket;

/**
 * AliveSender
 *
 * @author ms
 * @since 2016-01-14
 */
public class AliveSender implements Runnable {

    /**
     * Constant to send a 'Heart Beat' every 30 seconds.
     */
    private static final int TIMERMILLIS = 30000;

    private final WebSocket.Out<String> socket;
    private boolean done;
    private final Thread thread;


    public AliveSender(WebSocket.Out<String> socket) {
        this.socket = socket;
        this.done = false;
        this.thread = new Thread(this, "HeartBeater");
        this.thread.start();
    }

    public void setDone() {
        this.done = true;
        this.thread.interrupt();
    }

    @Override
    public void run() {
        while (!done) {
            try {
                Thread.sleep(TIMERMILLIS);
                this.socket.write(new AliveMessage().toJSON());
            } catch (InterruptedException e) {
                // ignore interrupt, is send by this.setDone()
            }
        }
    }
}
