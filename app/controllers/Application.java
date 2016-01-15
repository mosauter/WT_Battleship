package controllers;


import controllers.util.GameInstance;
import de.htwg.battleship.Battleship;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.WebSocket;
import views.html.game;
import views.html.home;

import java.util.LinkedList;
import java.util.List;

public class Application extends Controller {

    /**
     * List of {@link GameInstance} with one player waiting for an opponent.
     */
    private static final List<GameInstance> onePlayer = new LinkedList<>();

    private static int anInt = 0;

    public Result index() {
        return ok(home.render());
    }

    public Result game() {
        return ok(game.render());
    }

    public WebSocket<String> socket(String login) {
        return new WebSocket<String>() {
            private boolean firstPlayer;
            private GameInstance instance;
            private WuiController wuiController;

            public void onReady(WebSocket.In<String> in, WebSocket.Out<String> out) {
                if (!onePlayer.isEmpty()) {
                    GameInstance gameInstance = onePlayer.get(0);
                    onePlayer.remove(0);
                    gameInstance.setSocketTwo(out);
                    this.wuiController = new WuiController(gameInstance
                        .getInstance().getMasterController(), out, false);
                    gameInstance.setWuiControllerTwo(this.wuiController);
                    firstPlayer = false;
                } else {
                    Battleship battleship = Battleship.getInstance(true);
                    this.wuiController = new WuiController(battleship
                        .getMasterController(), out, true);
                    this.instance = new GameInstance(battleship, out, this.wuiController);
                    onePlayer.add(this.instance);
                    this.wuiController.startGame();
                    firstPlayer = true;
                }
                this.wuiController.setName(anInt++ + login);

                in.onMessage(event -> this.wuiController.analyzeMessage(event));

                in.onClose(() -> {
                    System.out.println("CLOSING SOCKET");
                    this.instance.closedSocket(firstPlayer);
                    // removing from list if there was only one instance in the list
                    onePlayer.remove(this.instance);
                });
            }
        };
    }
}
