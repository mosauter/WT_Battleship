package controllers;

import org.pac4j.core.profile.CommonProfile;
import org.pac4j.core.profile.ProfileManager;
import org.pac4j.play.PlayWebContext;
import org.pac4j.play.java.Secure;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.WebSocket;

import java.util.List;

public class Auth extends Controller {

    private static final String NAME_TAG = "name";
    private static final String ID_TAG = "sub";

    public String getProfileAttribute(String attribute) {
        final PlayWebContext context = new PlayWebContext(ctx());
        final ProfileManager<CommonProfile> profileManager = new ProfileManager<>(context);
        List<CommonProfile> profiles = profileManager.getAll(true);
        if (profiles.isEmpty()) {
            return "";
        }
        CommonProfile profile = profiles.get(0);
        try {
            return profile.getAttribute(attribute).toString();
        } catch (Exception e) {
            // ignore
        }
        return "";
    }

    @Secure(clients = "OidcClient")
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

    public Result presentationWeb() {
        Application application = new Application();
        return application.presentationWeb(this.getProfileAttribute(NAME_TAG));
    }

    public Result presentationArch() {
        Application application = new Application();
        return application.presentationArch(this.getProfileAttribute(NAME_TAG));
    }

    @Secure(clients = "OidcClient")
    public Result authenticate(String redirectUrl) {
        return redirect("/" + redirectUrl);
    }

    @Secure(clients = "OidcClient")
    public WebSocket<String> socketAuth() {
        Application app = new Application();
        return app.socket(this.getProfileAttribute(NAME_TAG), this.getProfileAttribute(ID_TAG));
    }
}
