@Override
protected void configure(){
    // OpenID Connect
    final OidcClient oidcClient = new OidcClient();
    oidcClient.setClientID(GOOGLE_CLIENT_ID);
    oidcClient.setSecret(GOOGLE_CLIENT_SECRET);
    oidcClient.setDiscoveryURI(GOOGLE_DISCOVERY_URI);
    oidcClient.addCustomParam("prompt", "consent");

    // ...
}
