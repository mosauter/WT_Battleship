package controllers;


import de.htwg.battleship.Battleship;

import de.htwg.battleship.aview.tui.TUI;
import play.*;
import play.mvc.*;
import views.html.*;
import play.mvc.WebSocket;
import play.mvc.WebSocket.*;
import play.libs.F.Callback;
import play.libs.F.Callback0;

public class Application extends Controller {

    static Battleship bs = Battleship.getInstance();

    public Result index() {
        return ok(index.render(about.render("Battleship POW! POW!")));
    }

    public Result wui(String command) {
        TUI tui = bs.getTui();
        tui.processInputLine(command);
        String s = tui.printTUI();
        String b = s.replaceAll("\n","<br>" );
        b = b.replaceAll(" ","&nbsp;" );

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
            public void onReady(WebSocket.In<String> in, WebSocket.Out<String> out) {
                in.onMessage(new Callback<String>() {
                    public void invoke(String event) {
                        System.out.println("Some cool event came: " + event);
                    }
                });
                in.onClose(new Callback0() {
                    public void invoke() {
                        System.out.println("CLOSING SOCKET");
                    }
                });
                out.write("{\"object\":\"ghjkl\",\"zahl\":14}");
            }
        };
    }
}
