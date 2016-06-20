package controllers.util.messages;

import controllers.util.WebGameSave;
import de.htwg.battleship.model.IPlayer;

public class SaveGameMessage extends Message {

    private final WebGameSave[] saveGames;
    private final IPlayer player;

    public SaveGameMessage(WebGameSave[] gameSaves, IPlayer player) {
        this.type = "SAVEGAMES";
        this.saveGames = gameSaves;
        this.player = player;
    }
}
