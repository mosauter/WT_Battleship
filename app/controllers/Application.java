package controllers;


import controllers.util.GameInstance;
import de.htwg.battleship.Battleship;
import de.htwg.battleship.aview.tui.TUI;
import org.apache.maven.wagon.authorization.AuthorizationException;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.WebSocket;
import views.html.game;
import views.html.home;

import java.util.LinkedList;
import java.util.List;

/*from google oAuth:*/
import model.JsonContent;

import modules.SecurityModule;
import org.pac4j.core.client.Clients;
import org.pac4j.core.client.IndirectClient;
import org.pac4j.core.exception.TechnicalException;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.core.profile.UserProfile;
import org.pac4j.http.client.indirect.FormClient;
import org.pac4j.jwt.profile.JwtGenerator;
import org.pac4j.play.PlayWebContext;
import org.pac4j.play.java.RequiresAuthentication;

import org.pac4j.play.java.UserProfileController;
import play.mvc.Result;
import play.twirl.api.Content;



public class Application extends Controller {
//public class Application extends UserProfileController<CommonProfile> {

    /*Start google oAuth methods*/

    /*End google oAuth methods*/


    /**
     * List of {@link GameInstance} with one player waiting for an opponent.
     */
    private List<GameInstance> onePlayer = new LinkedList<>();

    static Battleship bs = Battleship.getInstance();

    //ACHTUNG: Die Home-Seite IST ZU TESTZWECKEN DEAKTIVIERT
    /*
    public Result index() {
        return home();
    }
    */
    public Result wui(String command) {
        TUI tui = bs.getTui();
        tui.processInputLine(command);
        String s = tui.printTUI();
        String b = s.replaceAll("\n", "<br>");
        b = b.replaceAll(" ", "&nbsp;");

        return ok(views.html.battleship.render(b));
    }

    public Result home() {
        return ok(home.render("DUMMY"));
    }

    public Result game(String username) {
        return ok(game.render(username));
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
}
