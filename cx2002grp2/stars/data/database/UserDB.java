package cx2002grp2.stars.data.database;

import cx2002grp2.stars.data.dataitem.User;

public class UserDB extends SingleStringKeyDatabase<User> {

    private static final String DB_FILE_PATH = "tables/user.csv";

    private static UserDB instance = new UserDB();

    private UserDB() {
        loadData();
    }

    public static UserDB getDB() {
        return instance;
    }

    protected void loadData() {
        // TODO - implement UserDB.loadData
    }

    protected void saveData() {
        // TODO - implement UserDB.saveData
    }

}