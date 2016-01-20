package controllers;


import controllers.util.GameInstance;
import de.htwg.battleship.Battleship;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.WebSocket;
import views.html.about;
import views.html.game;
import views.html.home;
import views.html.about;
import views.html.googlePage;

import java.util.LinkedList;
import java.util.List;

public class Application extends Controller {

    /**
     * List of {@link GameInstance} with one player waiting for an opponent.
     */
    private static final List<GameInstance> onePlayer = new LinkedList<>();

    public Result home() {
        return ok(home.render("Login"));
    }

    public Result about() {
        return ok(about.render("Login"));
    }

    public Result game(String username) {
        return ok(game.render(username));
    }

    public Result about(String username) {
        return ok(about.render(username));
    }

    //no "Login" will appear in navbar
    public Result googlePage() {
        return ok(googlePage.render(" "));
    }

    public WebSocket<String> socket(String login) {
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
                this.wuiController.setName(login);

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
}
