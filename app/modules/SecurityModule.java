package modules;

import com.google.inject.AbstractModule;
import controllers.CustomAuthorizer;
import controllers.HttpStatusRedirector;
import org.pac4j.core.authorization.RequireAnyRoleAuthorizer;
import org.pac4j.core.client.Clients;
import org.pac4j.core.config.Config;
import org.pac4j.oidc.client.OidcClient;
import org.pac4j.play.ApplicationLogoutController;
import org.pac4j.play.CallbackController;
import play.Configuration;
import play.Environment;

/**
 * Security module will process the credentials for google and facebook platforms. Redirect url's must be defined in
 * respective platforms.
 */
public class SecurityModule extends AbstractModule {

    /**
     * Client ID from google for localhost.
     */
    //    private static final String GOOGLE_CLIENT_ID =
    //        "188113288901-3b17lnh2t56toiknn3nj7hk4icafvkl7" + ".apps.googleusercontent.com";
    /**
     * Client ID from google for heroku.
     */
    private static final String GOOGLE_CLIENT_ID =
        "188113288901-vpm2o01esoepnfc05nf9dmr7vn1rc8rd.apps.googleusercontent.com";
    /**
     * Client Secret from google for localhost.
     */
    //    private static final String GOOGLE_CLIENT_SECRET = "nRSM_56nUdPzd201SOJZffA9";
    /**
     * Client Secret from google for heroku.
     */
    private static final String GOOGLE_CLIENT_SECRET = "OvwKn9wJDWujGHdH1-bsIruT";
    public static final String GOOGLE_DISCOVERY_URI = "https://accounts.google.com/.well-known/openid-configuration";

    private final Environment environment;
    private final Configuration configuration;

    public SecurityModule(Environment environment, Configuration configuration) {
        this.environment = environment;
        this.configuration = configuration;
    }

    @Override
    protected void configure() {
        // OpenID Connect
        final OidcClient oidcClient = new OidcClient();
        oidcClient.setClientID(GOOGLE_CLIENT_ID);
        oidcClient.setSecret(GOOGLE_CLIENT_SECRET);
        oidcClient.setDiscoveryURI(GOOGLE_DISCOVERY_URI);
        oidcClient.addCustomParam("prompt", "consent");

        final Clients clients = new Clients(configuration.getString("baseUrl") + "/callback", oidcClient);

        final Config config = new Config(clients);
        config.addAuthorizer("admin", new RequireAnyRoleAuthorizer("ROLE_ADMIN"));
        config.addAuthorizer("custom", new CustomAuthorizer());
        config.setHttpActionAdapter(new HttpStatusRedirector());
        bind(Config.class).toInstance(config);

        // callback
        final CallbackController callbackController = new CallbackController();
        callbackController.setDefaultUrl("/");
        bind(CallbackController.class).toInstance(callbackController);
        // logout
        final ApplicationLogoutController logoutController = new ApplicationLogoutController();
        logoutController.setDefaultUrl("/");
        bind(ApplicationLogoutController.class).toInstance(logoutController);
    }
}
