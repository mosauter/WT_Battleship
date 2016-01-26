public class WaitMessage extends Message {
    private final String yourName;
    private final String opponentName;

    public WaitMessage(String yourName, String opponentName) {
        this.yourName = yourName;
        this.opponentName = opponentName;
        this.type = "WAIT";
    }
}
