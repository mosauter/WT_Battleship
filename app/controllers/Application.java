package controllers;


import controllers.util.GameInstance;
import de.htwg.battleship.Battleship;
import de.htwg.battleship.aview.tui.TUI;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.WebSocket;
import views.html.game;
import views.html.home;
import views.html.*;
import models.*;

import java.util.LinkedList;
import java.util.List;


public class Application extends Controller {

    /**
     * List of {@link GameInstance} with one player waiting for an opponent.
     */
    private List<GameInstance> onePlayer = new LinkedList<>();

    static Battleship bs = Battleship.getInstance();

    public Result index() {
        return home();
    }

    public Result wui(String command) {
        TUI tui = bs.getTui();
        tui.processInputLine(command);
        String s = tui.printTUI();
        String b = s.replaceAll("\n", "<br>");
        b = b.replaceAll(" ", "&nbsp;");

        return ok(views.html.battleship.render(b));
    }

    public Result home() {
        return ok(home.render());
    }

    public Result game() {
        return ok(game.render());
    }

    public WebSocket<String> socket(String login) {
        return new WebSocket<String>() {
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
                } else {
                    Battleship battleship = Battleship.getInstance();
                    this.wuiController = new WuiController(battleship
                        .getMasterController(), out, true);
                    this.instance = new GameInstance(battleship, out, this.wuiController);
                    onePlayer.add(this.instance);
                    this.wuiController.startGame();
                }
                this.wuiController.setName(login);

                in.onMessage(event -> {
                    this.wuiController.analyzeMessage(event);
                });

                in.onClose(() -> System.out.println("CLOSING SOCKET"));
            }
        };
    }



    // render chat page
    public static Result chat() {
        return ok(chat.render());
    }


    // get the ws.js script
    public static Result wsJs() {
        return ok(views.js.ws.render());
    }

    // Websocket interface
    public static WebSocket<String> wsInterface(){
        return new WebSocket<String>(){

            // called when websocket handshake is done
            public void onReady(WebSocket.In<String> in, WebSocket.Out<String> out){
                Chat.start(in, out);
            }
        };
    }


}
