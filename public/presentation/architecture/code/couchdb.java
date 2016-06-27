@View(name = "all", map = "function(doc) { emit(null, doc) }")
public class CouchRepository extends CouchDbRepositorySupport<CouchDbGameSave> {
    private static final String FIND_ALL = "find_all_by_player";
    private static final String FIND_GAMES = "find_games_by_players";

    public CouchRepository(Class<CouchDbGameSave> type, CouchDbConnector db, boolean createIfNotExits) {
        super(type, db, createIfNotExits);
        initStandardDesignDocument();
    }

    @View(name = FIND_ALL, map = "function(doc) {" +
            "emit(doc.player1ID, doc);" +
            "emit(doc.player2ID, doc); }")
    public List<CouchDbGameSave> findByPlayer(final IPlayer player) {
        return queryView(FIND_ALL, player.getID());
    }

    @View(name = FIND_GAMES, map = "function(doc) {" +
            "emit([doc.player1ID, doc.player2ID], doc); " +
            "emit([doc.player2ID, doc.player1ID], doc); }")
    public List<CouchDbGameSave> findByPlayers(final IPlayer player1, final IPlayer player2) {
        ComplexKey complexKey = ComplexKey.of(player1.getID(), player2.getID());
        return queryView(FIND_GAMES, complexKey);
    }
}