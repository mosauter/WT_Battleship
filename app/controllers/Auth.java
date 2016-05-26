package controllers;

import org.pac4j.core.profile.CommonProfile;
import org.pac4j.play.java.RequiresAuthentication;
import org.pac4j.play.java.UserProfileController;
import play.mvc.Result;
import play.mvc.WebSocket;

public class Auth extends UserProfileController<CommonProfile> {

    private static final String NAME_TAG = "name";
    private static final String ID_TAG = "sub";

    public String getProfileAttribute(String attribute) {
        try {
            final CommonProfile profile = getUserProfile();
            return profile.getAttribute(attribute).toString();
        } catch (Exception e) {
            // ignore
        }
        return "";
    }

    @RequiresAuthentication(clientName = "OidcClient")
    public Result game() {
        Application app = new Application();
        return app.game(this.getProfileAttribute(NAME_TAG));
    }

    public Result about() {
        Application app = new Application();
        return app.about(this.getProfileAttribute(NAME_TAG));
    }

    public Result home() {
        Application app = new Application();
        return app.home(this.getProfileAttribute(NAME_TAG));
    }

    public Result presentation() {
        Application application = new Application();
        return application.presentation(this.getProfileAttribute(NAME_TAG));
    }

    @RequiresAuthentication(clientName = "OidcClient")
    public Result authenticate(String redirectUrl) {
        return redirect("/" + redirectUrl);
    }

    @RequiresAuthentication(clientName = "OidcClient")
    public WebSocket<String> socketAuth() {
        Application app = new Application();
        return app.socket(this.getProfileAttribute(NAME_TAG), this.getProfileAttribute(ID_TAG));
    }
}
