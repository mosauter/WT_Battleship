package modules;

import com.google.inject.AbstractModule;
import controllers.CustomAuthorizer;
import controllers.DemoHttpActionAdapter;
import org.pac4j.cas.client.CasClient;
import org.pac4j.core.authorization.RequireAnyRoleAuthorizer;
import org.pac4j.core.client.Clients;
import org.pac4j.core.config.Config;
import org.pac4j.http.client.direct.DirectBasicAuthClient;
import org.pac4j.http.client.direct.ParameterClient;
import org.pac4j.http.client.indirect.FormClient;
import org.pac4j.http.client.indirect.IndirectBasicAuthClient;
import org.pac4j.http.credentials.authenticator.test.SimpleTestUsernamePasswordAuthenticator;
import org.pac4j.jwt.credentials.authenticator.JwtAuthenticator;
import org.pac4j.oauth.client.FacebookClient;
import org.pac4j.oauth.client.TwitterClient;
import org.pac4j.oidc.client.OidcClient;
import org.pac4j.play.ApplicationLogoutController;
import org.pac4j.play.CallbackController;
import org.pac4j.play.cas.logout.PlayCacheLogoutHandler;
import org.pac4j.saml.client.SAML2Client;
import org.pac4j.saml.client.SAML2ClientConfiguration;
import play.Configuration;
import play.Environment;

import java.io.File;

/**Security module will process the credentials for google and facebook platforms.
 * Redirect url's must be defined in respective platforms.
 */
public class SecurityModule extends AbstractModule {

    public final static String JWT_SALT = "12345678901234567890123456789012";

    private final Environment environment;
    private final Configuration configuration;

    public SecurityModule(
            Environment environment,
            Configuration configuration) {
        this.environment = environment;
        this.configuration = configuration;
    }

    @Override
    protected void configure() {
        final String fbId = configuration.getString("fbId");
        final String fbSecret = configuration.getString("fbSecret");
        final String baseUrl = configuration.getString("baseUrl");

        // OAuth
        final FacebookClient facebookClient = new FacebookClient(fbId, fbSecret);
        final TwitterClient twitterClient = new TwitterClient("HVSQGAw2XmiwcKOTvZFbQ",
                "FSiO9G9VRR4KCuksky0kgGuo8gAVndYymr4Nl7qc8AA");
        // HTTP
        final FormClient formClient = new FormClient(baseUrl + "/loginForm", new SimpleTestUsernamePasswordAuthenticator());
        final IndirectBasicAuthClient indirectBasicAuthClient = new IndirectBasicAuthClient(new SimpleTestUsernamePasswordAuthenticator());

        // CAS
        final CasClient casClient = new CasClient("https://casserverpac4j.herokuapp.com/login");
        casClient.setLogoutHandler(new PlayCacheLogoutHandler());
        casClient.setCasProtocol(CasClient.CasProtocol.CAS20);
        // casClient.setGateway(true);
        /*final CasProxyReceptor casProxyReceptor = new CasProxyReceptor();
        casProxyReceptor.setCallbackUrl("http://localhost:9000/casProxyCallback");
        casClient.setCasProxyReceptor(casProxyReceptor);*/

        // SAML
        final SAML2ClientConfiguration cfg = new SAML2ClientConfiguration("resource:samlKeystore.jks",
                "pac4j-demo-passwd", "pac4j-demo-passwd", "resource:openidp-feide.xml");
        cfg.setMaximumAuthenticationLifetime(3600);
        cfg.setServiceProviderEntityId("urn:mace:saml:pac4j.org");
        cfg.setServiceProviderMetadataPath(new File("target", "sp-metadata.xml").getAbsolutePath());
        final SAML2Client saml2Client = new SAML2Client(cfg);

        // OpenID Connect
        final OidcClient oidcClient = new OidcClient();
        oidcClient.setClientID("188113288901-3b17lnh2t56toiknn3nj7hk4icafvkl7.apps.googleusercontent.com");
        oidcClient.setSecret("nRSM_56nUdPzd201SOJZffA9");
        oidcClient.setDiscoveryURI("https://accounts.google.com/.well-known/openid-configuration");
        oidcClient.addCustomParam("prompt", "consent");

        // REST authent with JWT for a token passed in the url as the token parameter
        ParameterClient parameterClient = new ParameterClient("token", new JwtAuthenticator(JWT_SALT));
        parameterClient.setSupportGetRequest(true);
        parameterClient.setSupportPostRequest(false);

        // basic auth
        final DirectBasicAuthClient directBasicAuthClient = new DirectBasicAuthClient(new SimpleTestUsernamePasswordAuthenticator());

        final Clients clients = new Clients(baseUrl + "/callback", facebookClient, twitterClient, formClient,
                indirectBasicAuthClient, casClient, saml2Client, oidcClient, parameterClient, directBasicAuthClient); // , casProxyReceptor);

        final Config config = new Config(clients);
        config.addAuthorizer("admin", new RequireAnyRoleAuthorizer("ROLE_ADMIN"));
        config.addAuthorizer("custom", new CustomAuthorizer());
        config.setHttpActionAdapter(new DemoHttpActionAdapter());
        bind(Config.class).toInstance(config);
        
        // for test purposes: profile timeout = 60 seconds
        //((PlayCacheStore) config.getSessionStore()).setProfileTimeout(60);

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
