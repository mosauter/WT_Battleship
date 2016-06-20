// WuiController

package controllers;

import controllers.util.AliveSender;
import controllers.util.GameInstance;
import controllers.util.messages.*;
import de.htwg.battleship.controller.IMasterController;
import de.htwg.battleship.model.IPlayer;
import de.htwg.battleship.observer.IObserver;
import de.htwg.battleship.util.StatCollection;
import de.htwg.battleship.util.State;
import play.mvc.WebSocket;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Semaphore;

/**
 * WuiController is the controller which is used to communicate between the Web-Interface and the {@link
 * IMasterController}. It sends {@link Message} to the client and analyzes instructions by the client in the {@link
 * WuiController#analyzeMessage(String)}-Method.
 *
 * @author ms
 * @since 2015-11-25
 */
public class WuiController implements IObserver {

    /**
     * The String which is used to determine if the ship in the place instruction by the client is set with horizontal
     * orientation. If the String doesn't matches this means the ship is placed with vertical orientation.
     */
    private static final String HORIZONTAL_ORIENTATION = "true";

    /**
     * The {@link IMasterController} of the corresponding game instance {@link de.htwg.battleship.Battleship}.
     */
    private final IMasterController masterController;
    /**
     * The {@link play.mvc.WebSocket.Out<String>} which is used to communicate with the client.
     */
    private final WebSocket.Out<String> socket;
    /**
     * Indicates if this {@link WuiController} is assigned to the first or the second player.
     */
    private final boolean firstPlayer;
    /**
     * List which is used to buffer place ship instructions if the instruction came when it wasn't the player's turn.
     */
    private final List<String[]> bufferedPlaceList;
    /**
     * Indicates if the first player of the corresponding {@link de.htwg.battleship.Battleship} has finished placing. So
     * the {@link State} is after {@link State#FINALPLACE1}.
     */
    private boolean placeOneFinished = false;
    /**
     * The 'Heart Beater' which is used to keep the connection alive. It sends a {@link AliveMessage} every {@link
     * AliveSender#TIMERMILLIS} milli-seconds.
     */
    private final AliveSender aliveSender;
    private GameInstance gameInstance;

    private final Semaphore addShip;

    public WuiController(IMasterController masterController, WebSocket.Out<String> socket, boolean first) {
        this.masterController = masterController;
        this.socket = socket;
        this.firstPlayer = first;
        this.bufferedPlaceList = new LinkedList<>();
        masterController.addObserver(this);
        this.send(new FirstPlayerMessage(first));
        this.addShip = new Semaphore(1);
        this.aliveSender = new AliveSender(socket);
    }

    @Override
    public void update() {
        if (firstPlayer) {
            checkFirst();
        } else {
            checkSecond();
        }
    }

    public void setGameInstance(GameInstance gameInstance) {
        this.gameInstance = gameInstance;
    }

    public void chat(ChatMessage message) {
        this.send(message);
    }

    /**
     * Utility-Method to send a {@link Message} with the {@link WuiController#socket} to the corresponding client.
     *
     * @param msg the message which should be sent
     */
    private void send(Message msg) {
        if (msg != null) {
            socket.write(msg.toJSON());
        }
    }

    /**
     * Method to analyze a message by the client.
     *
     * @param message 'x y orientation' - to place a ship on the field (x/y) 'x y'             - to shoot on the field
     *                (x/y) 'CHAT message'    - to chat with each other
     */
    public void analyzeMessage(String message) {
        if (message.startsWith(GameInstance.CHAT_PREFIX)) {
            this.gameInstance.chat(message, firstPlayer);
            return;
        }
        String[] field = message.split(" ");
        if (field.length == 3) {
            // x y orientation -> which player
            placeShip(field);
        } else if (field.length == 2) {
            // x y -> test which player
            shoot(field);
        } else {
            send(new InvalidMessage());
        }
    }

    /**
     * Is used to process the saved instructions in the {@link WuiController#bufferedPlaceList}.
     *
     * @return true if a instruction was processed; false if the {@link WuiController#bufferedPlaceList} was empty
     */
    private boolean processPlaceList() {
        if (bufferedPlaceList.isEmpty()) {
            return false;
        }
        String[] instruction = bufferedPlaceList.get(0);
        bufferedPlaceList.remove(0);
        placeShip(instruction);
        return true;
    }

    /**
     * To place a ship on the field (x/y) with the specified orientation. Only places a ship if it's the players turn.
     * If it's not the players turn the instruction is buffered in the {@link WuiController#bufferedPlaceList}.
     *
     * @param field the instruction as string field: "['x', 'y', 'orientation']"
     */
    private void placeShip(String[] field) {
        if (firstPlayer && masterController.getCurrentState() == State.PLACE1 ||
            !firstPlayer && masterController.getCurrentState() == State.PLACE2) {
            try {
                addShip.acquire();
                masterController.placeShip(Integer.parseInt(field[0]), Integer.parseInt(field[1]),
                                           field[2].equals(HORIZONTAL_ORIENTATION));
                addShip.release();
            } catch (Exception e) {
                e.printStackTrace();
                // ignore
            }
        } else {
            bufferedPlaceList.add(field);
        }
    }

    /**
     * Used to shoot on the field (x/y).
     *
     * @param field the instruction as string field: "['x', 'y']"
     */
    private void shoot(String[] field) {
        if (firstPlayer && masterController.getCurrentState() == State.SHOOT1 ||
            !firstPlayer && masterController.getCurrentState() == State.SHOOT2) {
            masterController.shoot(Integer.parseInt(field[0]), Integer.parseInt(field[1]));
        }
        // else -> the player isn't allowed to shoot in this state
        // instruction is omitted
    }

    public void setProfile(String name, String id) {
        this.masterController.setPlayerProfile(name, Integer.parseInt(id));
    }

    public void startGame() {
        this.masterController.startGame();
    }

    private boolean[][] getShootMap(boolean[][] shootMap, IPlayer player) {
        boolean[][] hitMap = new boolean[masterController.getBoardSize()][masterController.getBoardSize()];
        Map<Integer, Set<Integer>> shipMap = getShipMap(player);
        for (Integer y : shipMap.keySet()) {
            for (Integer x : shipMap.get(y)) {
                hitMap[x][y] = shootMap[x][y];
            }
        }
        return hitMap;
    }

    private WinMessage createWinMessage(State state) {
        IPlayer winner = state == State.WIN1 ? masterController.getPlayer1() : masterController.getPlayer2();
        IPlayer looser = state == State.WIN1 ? masterController.getPlayer2() : masterController.getPlayer1();
        Map<Integer, Set<Integer>> winnerShips = getShipMap(winner);
        Map<Integer, Set<Integer>> looserShips = getShipMap(looser);

        // shoot map -> winner shoots at looser
        boolean[][] looserShootMap = winner.getOwnBoard().getHitMap();
        boolean[][] winnerShootMap = looser.getOwnBoard().getHitMap();

        return new WinMessage(state, winner, winnerShips, winnerShootMap, looser, looserShips, looserShootMap);
    }

    private ShootMessage createShootMessage(State state, IPlayer self, IPlayer opponent) {
        boolean[][] shootMap = opponent.getOwnBoard().getHitMap();
        boolean[][] hitMap = getShootMap(shootMap, opponent);
        boolean[][] opponentShootMap = self.getOwnBoard().getHitMap();
        return new ShootMessage(state, shootMap, hitMap, opponentShootMap);
    }

    private Map<Integer, Set<Integer>> getShipMap(IPlayer player) {
        Map<Integer, Set<Integer>> shipMap = StatCollection.createMap(masterController.getBoardSize());
        masterController.fillMap(player.getOwnBoard().getShipList(), shipMap, player.getOwnBoard().getShips());
        return shipMap;
    }

    private void checkFirst() {
        Message msg = null;
        State currentState = masterController.getCurrentState();
        switch (currentState) {
            case GETNAME2:
                msg = createWaitMessage();
                // if this is the second player
                // msg = null and nothing is sent
                break;

            // PLACING SHIPS
            case PLACE1:
                if (processPlaceList()) {
                    return;
                }
            case FINALPLACE1:
                this.placeOneFinished = currentState == State.FINALPLACE1;
                Map<Integer, Set<Integer>> shipMap = getShipMap(masterController.getPlayer1());
                msg = new PlaceMessage(currentState, shipMap);
                break;
            case PLACE2:
            case FINALPLACE2:
                msg = createWaitMessage();
                break;
            case PLACEERR:
                if (!placeOneFinished) {
                    msg = new PlaceErrorMessage(masterController.getPlayer1().getOwnBoard().getShips() + 2);
                }
                break;

            // SHOOTING ON EACH OTHER
            case SHOOT1:
            case SHOOT2:
                // opponent = player 2
                msg = createShootMessage(currentState, masterController.getPlayer1(), masterController.getPlayer2());
                break;

            case HIT:
            case MISS:
                msg = new HitMessage(currentState == State.HIT);
                break;

            // SOMEONE HAS WON
            case WIN1:
            case WIN2:
                msg = createWinMessage(currentState);
                break;

            // SOME INVALID INPUT
            case WRONGINPUT:
                msg = new InvalidMessage();
                break;

            default:
                break;
        }
        this.send(msg);
    }

    private void checkSecond() {
        Message msg = null;
        State currentState = masterController.getCurrentState();
        switch (currentState) {
            // PLACING SHIPS
            case PLACE1:
            case FINALPLACE1:
                this.placeOneFinished = currentState == State.FINALPLACE1;
                msg = createWaitMessage();
                break;
            case PLACE2:
                if (processPlaceList()) {
                    return;
                }
            case FINALPLACE2:
                Map<Integer, Set<Integer>> shipMap = getShipMap(masterController.getPlayer2());
                msg = new PlaceMessage(currentState, shipMap);
                break;
            case PLACEERR:
                if (placeOneFinished) {
                    // minimum in PLACE2
                    msg = new PlaceErrorMessage(masterController.getPlayer2().getOwnBoard().getShips() + 2);
                }
                break;

            // SHOOTING ON EACH OTHER
            case SHOOT1:
            case SHOOT2:
                // opponent = player 1
                msg = createShootMessage(currentState, masterController.getPlayer2(), masterController.getPlayer1());
                break;
            case HIT:
                msg = new HitMessage(true);
                break;
            case MISS:
                msg = new HitMessage(false);
                break;

            // SOMEONE HAS WON
            case WIN1:
            case WIN2:
                msg = createWinMessage(currentState);
                break;

            // SOME INVALID INPUT
            case WRONGINPUT:
                msg = new InvalidMessage();
                break;

            default:
                break;
        }
        this.send(msg);
    }

    private WaitMessage createWaitMessage() {
        if (firstPlayer) {
            return new WaitMessage(masterController.getPlayer1().getName(), masterController.getPlayer2().getName());
        }
        return new WaitMessage(masterController.getPlayer2().getName(), masterController.getPlayer1().getName());
    }

    public void setAliveDone() {
        aliveSender.setDone();
    }

    /**
     * If the other player has closed his socket this sends a {@link WinMessage} to the player, if it doesn't happened
     * already.
     */
    public void closedSocket() {
        this.aliveSender.setDone();
        if (masterController.getCurrentState() != State.END) {
            // sending Winn message -> other player left game / killed his socket before ending the game
            this.send(createWinMessage(firstPlayer ? State.WIN1 : State.WIN2));
        }
    }

    public IPlayer getPlayer() {
        return firstPlayer ? masterController.getPlayer1() : masterController.getPlayer2();
    }
}
