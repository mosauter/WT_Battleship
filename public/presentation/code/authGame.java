@RequiresAuthentication(clientName = "OidcClient")
public Result game() {
    Application app = new Application();
    return app.game(this.getCurrentUsername());
}
