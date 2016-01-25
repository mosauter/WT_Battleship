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
