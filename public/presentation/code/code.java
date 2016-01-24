public void analyzeMessage(String message) {
    Logger.info("Received message:\n" + message);
    if (message.startsWith(GameInstance.CHAT_PREFIX)) {
        this.gameInstance.chat(message, firstPlayer);
        return;
    }
    String[] field = message.split(" ");
    if (field.length == 3) {
        // x y orientation -> which player
        placeShip(field);
    } else if (field.length == 2) {
        // x y -> test which player
        shoot(field);
    } else {
        send(new InvalidMessage());
    }
}
