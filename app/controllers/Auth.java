package controllers;


import modules.SecurityModule;
import org.pac4j.core.client.Clients;
import org.pac4j.core.exception.TechnicalException;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.core.profile.UserProfile;
import org.pac4j.http.client.indirect.FormClient;
import org.pac4j.jwt.profile.JwtGenerator;
import org.pac4j.play.PlayWebContext;
import org.pac4j.play.java.RequiresAuthentication;
import org.pac4j.play.java.UserProfileController;
import play.mvc.Result;
import play.mvc.WebSocket;

public class Auth extends UserProfileController<CommonProfile> {

    public Result index() throws Exception {
        // profile (maybe null if not authenticated)
        final CommonProfile profile = getUserProfile();
        final Clients clients = config.getClients();
        final PlayWebContext context =
            new PlayWebContext(ctx(), config.getSessionStore());
        final String urlOidc = "google";
        return ok(views.html.index.render(profile, urlOidc));
    }

    public String getCurrentUsername() {
        final CommonProfile profile = getUserProfile();
        return (String) profile.getAttribute("name");
    }

    @RequiresAuthentication(clientName = "OidcClient")
    public Result game() {
        Application app = new Application();
        return app.game(this.getCurrentUsername());
    }

    @RequiresAuthentication(clientName = "OidcClient")
    public WebSocket<String> socketAuth() {
        Application app = new Application();
        return app.socket(this.getCurrentUsername());
    }

    private Result protectedIndexView() {
        // profile
        final String username = this.getCurrentUsername();
        //return ok(views.html.protectedIndex.render(profile,username));
        return ok(views.html.namePage.render(username));
    }

    @RequiresAuthentication(clientName = "OidcClient")
    public Result oidcIndex() {
        return game();
    }

    public Result loginForm() throws TechnicalException {
        final FormClient formClient =
            (FormClient) config.getClients().findClient("FormClient");
        return ok(views.html.loginForm.render(formClient.getCallbackUrl()));
    }

    public Result jwt() {
        final UserProfile profile = getUserProfile();
        final JwtGenerator generator =
            new JwtGenerator(SecurityModule.JWT_SALT);
        String token = "";
        if (profile != null) {
            token = generator.generate(profile);
        }
        return ok(views.html.jwt.render(token));
    }
}
