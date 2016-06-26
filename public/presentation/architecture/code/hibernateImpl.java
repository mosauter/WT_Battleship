public class HibernateDAO extends IDAO {
    public HibernateDAO() {
        sessionFactory = HibernateUtil.getInstance();
        this.injector = Guice.createInjector(new BattleshipHibernateModule());
    }

    @Override
    public void saveOrUpdateGame(IMasterController masterController) {
        Transaction tx = null;
        try {
            Session session = sessionFactory.getCurrentSession();
            tx = session.beginTransaction();
            IGameSave save = injector.getInstance(IGameSave.class);
            save.saveGame(masterController);
            session.save(save);
            tx.commit();
        } catch (HibernateException ex) {
            handleHibernateException(ex, tx);
        }
    }
}
