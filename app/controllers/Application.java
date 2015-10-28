package controllers;


import de.htwg.battleship.Battleship;
import de.htwg.battleship.aview.tui.TUI;
import play.*;
import play.mvc.*;
import views.html.*;

public class Application extends Controller {

    private static Battleship battleship = Battleship.getInstance();

    public static Result index() {
        return ok(index.render(about.render("Battleship POW! POW!")));
    }

    public static Result wui(String command) {
        TUI tui = battleship.getTui();
        tui.processInputLine(command);
        return ok(views.html.battleship.render(tui.printTUI()));
    }

    //public static Result commandline(String command) {
      //  battleship.getTui().processInputLine(command);
        //return ok(views.html.index.render("Got your command "+ command, controller));
    //}

}
