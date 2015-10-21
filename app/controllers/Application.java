package controllers;

import de.htwg.battleship.Battleship;
import de.htwg.battleship.controller.IMasterController;
import play.*;
import play.mvc.*;

import views.html.*;

public class Application extends Controller {

    static IMasterController controller = Battleship.getInstance().getMaster();
    static Battleship battleship = Battleship.getInstance();
    public static Result index() {
        return ok(index.render(about.render("Battleship POW! POW!")));
    }

    //public static Result index() {
      //  return ok(views.html.index.render("Battleship", controller));
    //}

    //public static Result commandline(String command) {
      //  battleship.getTui().processInputLine(command);
        //return ok(views.html.index.render("Got your command "+ command, controller));
    //}

}
