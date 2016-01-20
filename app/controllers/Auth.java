package controllers;

import org.pac4j.core.profile.CommonProfile;
import org.pac4j.play.ApplicationLogoutController;
import org.pac4j.play.java.RequiresAuthentication;
import org.pac4j.play.java.UserProfileController;
import play.mvc.Result;
import play.mvc.WebSocket;
import views.html.login;

public class Auth extends UserProfileController<CommonProfile> {

    public Result index() {
        return ok(login.render(getCurrentUsername()));
    }

    public String getCurrentUsername() {
        try {
            final CommonProfile profile = getUserProfile();
            return (String) profile.getAttribute("name");
        } catch (Exception e) {
            e.printStackTrace();
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

    @RequiresAuthentication(clientName = "OidcClient")
    public Result authenticate(String redirectUrl) {
        System.out.println("Redirecting to: " + redirectUrl);
        return redirect("/" + redirectUrl);
    }

    @RequiresAuthentication(clientName = "OidcClient")
    public WebSocket<String> socketAuth() {
        Application app = new Application();
        return app.socket(this.getCurrentUsername());
    }

    @RequiresAuthentication(clientName = "OidcClient")
    public Result logout(String redirectUrl) {
        ApplicationLogoutController applicationLogoutController = new ApplicationLogoutController();
        applicationLogoutController.logout();
        return redirect("/" + redirectUrl);
    }
}
