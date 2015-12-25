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

    Auth auth =new Auth();
    public Result index() throws Exception {
        System.out.println("adsfadsf");
        return auth.index();
    }

    /*private Result protectedIndexView() {
        auth.protectedIndexView();
    }*/

    //@RequiresAuthentication(clientName = "FacebookClient")
    public Result facebookIndex() {
        return auth.facebookIndex();
    }

    //@RequiresAuthentication(clientName = "FacebookClient", authorizerName = "admin")
    public Result facebookAdminIndex() {
        return auth.facebookAdminIndex();
    }

    //@RequiresAuthentication(clientName = "FacebookClient", authorizerName = "custom")
    public Result facebookCustomIndex() {
        return auth.facebookCustomIndex();
    }

    //@RequiresAuthentication(clientName = "TwitterClient,FacebookClient")
    public Result twitterIndex() {
        return auth.twitterIndex();
    }

    //@RequiresAuthentication
    public Result protectedIndex() {
        return auth.protectedIndex();
    }

    //@RequiresAuthentication(clientName = "FormClient")
    public Result formIndex() {
        return auth.formIndex();
    }

    // Setting the isAjax parameter is no longer necessary as AJAX requests are automatically detected:
    // a 401 error response will be returned instead of a redirection to the login url.
   // @RequiresAuthentication(clientName = "FormClient")
    public Result formIndexJson() {
        return auth.formIndexJson();
    }

    //@RequiresAuthentication(clientName = "IndirectBasicAuthClient")
    public Result basicauthIndex() {
        return auth.basicauthIndex();
    }

    //@RequiresAuthentication(clientName = "DirectBasicAuthClient,ParameterClient")
    public Result dbaIndex() {
        return auth.dbaIndex();
    }

    //@RequiresAuthentication(clientName = "CasClient")
    public Result casIndex() {
        /*final CommonProfile profile = getUserProfile();
        final String service = "http://localhost:8080/proxiedService";
        String proxyTicket = null;
        if (profile instanceof CasProxyProfile) {
            final CasProxyProfile proxyProfile = (CasProxyProfile) profile;
            proxyTicket = proxyProfile.getProxyTicketFor(service);
        }
        return ok(views.html.casProtectedIndex.render(profile, service, proxyTicket));*/
        return auth.casIndex();
    }

    //@RequiresAuthentication(clientName = "SAML2Client")
    public Result samlIndex() {
        return auth.samlIndex();
    }

    //@RequiresAuthentication(clientName = "OidcClient")
    public Result oidcIndex() {
        return auth.oidcIndex();
    }

    //@RequiresAuthentication(clientName = "ParameterClient")
    public Result restJwtIndex() {
        return auth.restJwtIndex();
    }

    public Result loginForm(){
        return auth.loginForm();
    }

    public Result jwt() {
        return auth.jwt();
    }


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
}
