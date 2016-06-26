public class HibernateGameSave implements IGameSave, Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    @Column(name = "player1Name")
    private String player1Name;
    @Column(name = "player1ID")
    private String player1ID;
    @Column(name = "player2Name")
    private String player2Name;
    @Column(name = "player2ID")
    private String player2ID;
    @Column(name = "gameMode")
    private GameMode gameMode;
    @Column(name = "currentState")
    private State currentState;
    @Column(name = "field1", length = 660)
    private String field1;
    @Column(name = "field2", length = 660)
    private String field2;
    @Column(name = "shipList1")
    private IShip[] shipList1;
    @Column(name = "shipList2")
    private IShip[] shipList2;
    @Column(name = "heightLength")
    private int heightLength;
    @Column(name = "maxShipNumber")
    private int maxShipNumber;

    // ....
}
