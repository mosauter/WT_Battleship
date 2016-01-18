package controllers;

import org.pac4j.core.profile.CommonProfile;
import org.pac4j.play.java.RequiresAuthentication;
import org.pac4j.play.java.UserProfileController;
import play.mvc.Result;
import play.mvc.WebSocket;
import views.html.login;

public class Auth extends UserProfileController<CommonProfile> {

    public Result index() throws Exception {
        return ok(login.render());
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
}
