// DBAccessor

package controllers.util;

import de.htwg.battleship.Battleship;
import de.htwg.battleship.dao.IDAO;
import de.htwg.battleship.model.IPlayer;
import de.htwg.battleship.model.IShip;
import de.htwg.battleship.model.persistence.IGameSave;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * DBAccessor
 *
 * @author ms
 * @since 2016-06-20
 */
public class DBAccessor {
    private final static IDAO dao = Battleship.getInstance().getInjector().getInstance(IDAO.class);

    public static List<WebGameSave> getGames(IPlayer player) {
        List<WebGameSave> result = new LinkedList<>();
        List<IGameSave> saves = dao.listAllGames(player);
        for (IGameSave save : saves) {
            Map<Integer, Set<Integer>> shipMap1 = ShipMapper
                .translateShipMap(save.getShipList1(), countShips(save.getShipList1()), save.getHeightLength());
            Map<Integer, Set<Integer>> shipMap2 = ShipMapper
                .translateShipMap(save.getShipList2(), countShips(save.getShipList2()), save.getHeightLength());
            result.add(
                new WebGameSave(save.getPlayer1Name(), save.getPlayer1ID(), save.getPlayer2Name(), save.getPlayer2ID(),
                                save.getGameMode(), save.getCurrentState(), save.getField1(), save.getField2(),
                                shipMap1, shipMap2, save.getHeightLength(), save.getMaxShipNumber()));
        }
        return result;
    }

    private static int countShips(IShip[] list) {
        int i = 0;
        for (; i < list.length; i++) {
            if (list[i] == null) {
                break;
            }
        }
        return i;
    }
}
