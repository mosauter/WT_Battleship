// ShipMapper

package controllers.util;

import de.htwg.battleship.model.IShip;
import de.htwg.battleship.util.StatCollection;

import java.util.Map;
import java.util.Set;

/**
 * ShipMapper
 *
 * @author ms
 * @since 2016-06-20
 */
public class ShipMapper {
    public static Map<Integer, Set<Integer>> translateShipMap(IShip[] shipList, int shipCount, int boardSize) {
        Map<Integer, Set<Integer>> shipMap = StatCollection.createMap(boardSize);
        fillMap(shipList, shipMap, shipCount);
        return shipMap;
    }

    private static Map<Integer, Set<Integer>> fillMap(final IShip[] shipList, final Map<Integer, Set<Integer>> map,
                                                      final int ships) {
        for (int i = 0; i < ships; i++) {
            getSet(shipList[i], map);
        }
        return map;
    }

    private static Map<Integer, Set<Integer>> getSet(final IShip ship, final Map<Integer, Set<Integer>> map) {
        if (ship.isOrientation()) {
            int xlow = ship.getX();
            int xupp = xlow + ship.getSize();
            Set<Integer> set = map.get(ship.getY());
            for (int i = xlow; i < xupp; i++) {
                set.add(i);
            }
            return map;
        } else {
            int ylow = ship.getY();
            int yupp = ylow + ship.getSize();
            int x = ship.getX();
            for (int i = ylow; i < yupp; i++) {
                map.get(i).add(x);
            }
            return map;
        }
    }
}
