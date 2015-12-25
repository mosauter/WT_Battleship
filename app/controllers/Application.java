package controllers;


import controllers.util.GameInstance;
import de.htwg.battleship.Battleship;
import de.htwg.battleship.aview.tui.TUI;
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


//public class Application extends Controller {
public class Application extends UserProfileController<CommonProfile> {

    /*Start google oAuth methods*/


    public Result index() throws Exception {
        // profile (maybe null if not authenticated)
        final CommonProfile profile = getUserProfile();
        final Clients clients = config.getClients();
        final PlayWebContext context = new PlayWebContext(ctx(), config.getSessionStore());
        final String urlFacebook = ((IndirectClient) clients.findClient("FacebookClient")).getRedirectAction(context, false).getLocation();
        final String urlTwitter = ((IndirectClient) clients.findClient("TwitterClient")).getRedirectAction(context, false).getLocation();
        final String urlForm = ((IndirectClient) clients.findClient("FormClient")).getRedirectAction(context, false).getLocation();
        final String urlBasicAuth = ((IndirectClient) clients.findClient("IndirectBasicAuthClient")).getRedirectAction(context, false).getLocation();
        final String urlCas = ((IndirectClient) clients.findClient("CasClient")).getRedirectAction(context, false).getLocation();
        //final String urlOidc = ((IndirectClient) clients.findClient("OidcClient")).getRedirectAction(context, false).getLocation();
        final String urlOidc = "google";
        final String urlSaml = ((IndirectClient) clients.findClient("SAML2Client")).getRedirectAction(context, false).getLocation();
        return ok(views.html.index.render(profile, urlFacebook, urlTwitter, urlForm, urlBasicAuth, urlCas, urlOidc,
                urlSaml));
    }

    private Result protectedIndexView() {
        // profile
        final CommonProfile profile = getUserProfile();
        final String username = profile.getFamilyName();

        //return ok(views.html.protectedIndex.render(profile,username));
        return ok(views.html.namePage.render(username));
    }

    @RequiresAuthentication(clientName = "FacebookClient")
    public Result facebookIndex() {
        return protectedIndexView();
    }

    @RequiresAuthentication(clientName = "FacebookClient", authorizerName = "admin")
    public Result facebookAdminIndex() {
        return protectedIndexView();
    }

    @RequiresAuthentication(clientName = "FacebookClient", authorizerName = "custom")
    public Result facebookCustomIndex() {
        return protectedIndexView();
    }

    @RequiresAuthentication(clientName = "TwitterClient,FacebookClient")
    public Result twitterIndex() {
        return protectedIndexView();
    }

    @RequiresAuthentication
    public Result protectedIndex() {
        return protectedIndexView();
    }

    @RequiresAuthentication(clientName = "FormClient")
    public Result formIndex() {
        return protectedIndexView();
    }

    // Setting the isAjax parameter is no longer necessary as AJAX requests are automatically detected:
    // a 401 error response will be returned instead of a redirection to the login url.
    @RequiresAuthentication(clientName = "FormClient")
    public Result formIndexJson() {
        final CommonProfile profile = getUserProfile();
        final String username = profile.getFamilyName();
        //Content content = views.html.protectedIndex.render(profile,username);
        Content content = views.html.namePage.render(username);
        JsonContent jsonContent = new JsonContent(content.body());
        return ok(jsonContent);
    }

    @RequiresAuthentication(clientName = "IndirectBasicAuthClient")
    public Result basicauthIndex() {
        return protectedIndexView();
    }

    @RequiresAuthentication(clientName = "DirectBasicAuthClient,ParameterClient")
    public Result dbaIndex() {
        return protectedIndexView();
    }

    @RequiresAuthentication(clientName = "CasClient")
    public Result casIndex() {
        /*final CommonProfile profile = getUserProfile();
        final String service = "http://localhost:8080/proxiedService";
        String proxyTicket = null;
        if (profile instanceof CasProxyProfile) {
            final CasProxyProfile proxyProfile = (CasProxyProfile) profile;
            proxyTicket = proxyProfile.getProxyTicketFor(service);
        }
        return ok(views.html.casProtectedIndex.render(profile, service, proxyTicket));*/
        return protectedIndexView();
    }

    @RequiresAuthentication(clientName = "SAML2Client")
    public Result samlIndex() {
        return protectedIndexView();
    }

    @RequiresAuthentication(clientName = "OidcClient")
    public Result oidcIndex() {
        return protectedIndexView();
    }

    @RequiresAuthentication(clientName = "ParameterClient")
    public Result restJwtIndex() {
        return protectedIndexView();
    }

    public Result loginForm() throws TechnicalException {
        final FormClient formClient = (FormClient) config.getClients().findClient("FormClient");
        return ok(views.html.loginForm.render(formClient.getCallbackUrl()));
    }

    public Result jwt() {
        final UserProfile profile = getUserProfile();
        final JwtGenerator generator = new JwtGenerator(SecurityModule.JWT_SALT);
        String token = "";
        if (profile != null) {
            token = generator.generate(profile);
        }
        return ok(views.html.jwt.render(token));
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
