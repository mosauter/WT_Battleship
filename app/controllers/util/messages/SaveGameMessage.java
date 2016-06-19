package controllers.util.messages;

import de.htwg.battleship.model.persistence.IGameSave;
import de.htwg.battleship.model.IPlayer;

public class SaveGameMessage extends Message {

    private final IGameSave[] saveGames;
    private final IPlayer player;

    public SaveGameMessage(IGameSave[] saveGames, IPlayer player) {
        this.type = "SAVEGAMES";
        this.saveGames = saveGames;
        this.player = player;
    }
}