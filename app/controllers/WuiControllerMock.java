// WuiControllerMock

package controllers;

import com.google.inject.Guice;
import controllers.util.HitMessage;
import controllers.util.InvalidMessage;
import controllers.util.PlaceMessage;
import controllers.util.ShootMessage;
import controllers.util.WinMessage;
import de.htwg.battleship.BattleshipModule;
import de.htwg.battleship.controller.IMasterController;
import de.htwg.battleship.model.IPlayer;
import de.htwg.battleship.util.StatCollection;
import play.mvc.WebSocket;

import java.util.Map;
import java.util.Set;

import static de.htwg.battleship.util.State.PLACE1;
import static de.htwg.battleship.util.State.PLACEERR;
import static de.htwg.battleship.util.State.SHOOT1;
import static de.htwg.battleship.util.State.WIN1;

/**
 * WuiControllerMock
 *
 * @author ms
 * @since 2015-11-25
 */
public class WuiControllerMock {

    private WebSocket.Out<String> socket;

    public WuiControllerMock(IMasterController masterController, WebSocket.Out<String> socket) {
        // masterController is not used in the mock, only for the api
        this.socket = socket;
    }

    public void analyzeMessage(String message) {
        String msg;
        Map<Integer, Set<Integer>> mp1 = StatCollection.createMap();
        mp1.get(1).add(0);
        mp1.get(1).add(1);
        mp1.get(1).add(2);
        mp1.get(1).add(3);
        mp1.get(1).add(4);
        Map<Integer, Set<Integer>> mp2 = StatCollection.createMap();
        mp2.get(1).add(0);
        mp2.get(1).add(1);
        mp2.get(1).add(2);
        mp2.get(1).add(3);
        mp2.get(1).add(4);
        boolean[][] isHitMap1 = new boolean
            [StatCollection.heightLenght][StatCollection.heightLenght];
        isHitMap1[1][0] = true;
        isHitMap1[1][1] = true;
        isHitMap1[1][2] = true;
        isHitMap1[1][3] = true;
        isHitMap1[1][4] = true;
        boolean[][] isHitMap2 = new boolean
            [StatCollection.heightLenght][StatCollection.heightLenght];
        isHitMap2[1][0] = true;
        isHitMap2[1][1] = true;
        isHitMap2[1][2] = true;
        isHitMap2[1][3] = true;
        isHitMap2[1][4] = true;

        IPlayer winner = Guice.createInjector(new BattleshipModule())
                              .getInstance(IPlayer.class);
        IPlayer looser = Guice.createInjector(new BattleshipModule())
                              .getInstance(IPlayer.class);
        switch (message) {
            case "HIT":
                msg = new HitMessage(true).toJSON();
                break;
            case "PLACE":
                msg = new PlaceMessage(PLACE1, mp1).toJSON();
                break;
            case "SHOOT":
                msg = new ShootMessage(SHOOT1, isHitMap1).toJSON();
                break;
            case "WIN":
                msg = new WinMessage(WIN1, winner, mp1, isHitMap1,
                    looser, mp2, isHitMap2).toJSON();
                break;
            default:
                msg = new InvalidMessage(PLACEERR).toJSON();
                break;
        }

        socket.write(msg);
    }

}
