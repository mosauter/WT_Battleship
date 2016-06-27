public class CouchDbGameSave extends CouchDbDocument implements IGameSave, Serializable {

    private String id;
    private String revision;
    private String player1Name;
    private String player1ID;
    private String player2Name;
    private String player2ID;
    private GameMode gameMode;
    private State currentState;
    private String field1;
    private String field2;
    private IShip[] shipList1;
    private IShip[] shipList2;
    private int heightLength;
    private int maxShipNumber;
    ...

}