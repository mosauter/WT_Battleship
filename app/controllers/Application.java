package controllers;


import de.htwg.battleship.Battleship;
import de.htwg.battleship.aview.tui.TUI;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.WebSocket;
import views.html.about;
import views.html.game;
import views.html.home;
import views.html.index;

public class Application extends Controller {

    static Battleship bs = Battleship.getInstance();

    public Result index() {
        return ok(index.render(about.render("Battleship POW! POW!")));
    }

    public Result wui(String command) {
        TUI tui = bs.getTui();
        tui.processInputLine(command);
        String s = tui.printTUI();
        String b = s.replaceAll("\n", "<br>");
        b = b.replaceAll(" ", "&nbsp;");

        return ok(views.html.battleship.render(b));
    }

    public Result home() {
        return ok(home.render());
    }

    public Result game() {
        return ok(game.render());
    }

    public WebSocket<String> socket() {
        return new WebSocket<String>() {
            public void onReady(WebSocket.In<String> in,
                                WebSocket.Out<String> out) {
                in.onMessage(event -> {
                    WuiControllerMock wuiControllerMock =
                        new WuiControllerMock(out);
                    wuiControllerMock.analyzeMessage(event);
                });
                in.onClose(() -> System.out.println("CLOSING SOCKET"));
            }
        };
    }
}
