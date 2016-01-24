package controllers;

import org.pac4j.core.profile.CommonProfile;
import org.pac4j.play.java.RequiresAuthentication;
import org.pac4j.play.java.UserProfileController;
import play.mvc.Result;
import play.mvc.WebSocket;
import views.html.login;

public class Auth extends UserProfileController<CommonProfile> {

    private static final String NAME_TAG = "name";

    public Result index() {
        return ok(login.render(getCurrentUsername()));
    }

    public String getCurrentUsername() {
        try {
            final CommonProfile profile = getUserProfile();
            return (String) profile.getAttribute(NAME_TAG);
        } catch (Exception e) {
        }
        return null;
    }

    @RequiresAuthentication(clientName = "OidcClient")
    public Result game() {
        Application app = new Application();
        return app.game(this.getCurrentUsername());
    }

    public Result about() {
        Application app = new Application();
        return app.about(this.getCurrentUsername());
    }

    public Result home() {
        Application app = new Application();
        return app.home(this.getCurrentUsername());
    }

    public Result instructions(){
        Application app = new Application();
        return app.instructions(this.getCurrentUsername());
    }

    public Result presentation() {
        Application application = new Application();
        return application.presentation(this.getCurrentUsername());
    }

    @RequiresAuthentication(clientName = "OidcClient")
    public Result authenticate(String redirectUrl) {
        return redirect("/" + redirectUrl);
    }

    @RequiresAuthentication(clientName = "OidcClient")
    public WebSocket<String> socketAuth() {
        Application app = new Application();
        return app.socket(this.getCurrentUsername());
    }
}
