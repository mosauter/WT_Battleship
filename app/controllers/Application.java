package controllers;


import controllers.util.GameInstance;
import de.htwg.battleship.Battleship;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.WebSocket;
import views.html.*;

import java.util.LinkedList;
import java.util.List;

public class Application extends Controller {

    /**
     * List of {@link GameInstance} with one player waiting for an opponent.
     */
    private static final List<GameInstance> onePlayer = new LinkedList<>();

    public Result home(String username) {
        return ok(home.render(username, ""));
    }

    public Result game(String username) {
        return ok(game.render(username));
    }

    public Result about(String username) {
        return ok(about.render(username));
    }

    public Result googlePage() {
        return ok(googlePage.render(" "));
    }

    public WebSocket<String> socket(String login, String id) {
        return new WebSocket<String>() {
            private boolean firstPlayer;
            private GameInstance instance;
            private WuiController wuiController;

            public void onReady(WebSocket.In<String> in,
                                WebSocket.Out<String> out) {
                if (onePlayer.isEmpty()) {
                    Battleship battleship = Battleship.getInstance(true);
                    this.wuiController =
                        new WuiController(battleship.getMasterController(), out,
                                          true);
                    this.instance =
                        new GameInstance(battleship, out, this.wuiController);
                    onePlayer.add(this.instance);
                    this.wuiController.startGame();
                    firstPlayer = true;
                } else {
                    this.instance = onePlayer.get(0);
                    onePlayer.remove(0);
                    this.instance.setSocketTwo(out);
                    this.wuiController = new WuiController(
                        this.instance.getInstance().getMasterController(), out,
                        false);
                    this.instance.setWuiControllerTwo(this.wuiController);
                    firstPlayer = false;
                }
                this.wuiController.setProfile(login, id);

                in.onMessage((String message) -> this.wuiController
                    .analyzeMessage(message));

                in.onClose(() -> {
                    try {
                        this.instance.closedSocket(firstPlayer);
                        // removing from list if there was only one instance in the list
                        onePlayer.remove(this.instance);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }
        };
    }

    public Result presentation(String currentUsername) {
        return ok(presentation.render(currentUsername));
    }
}
