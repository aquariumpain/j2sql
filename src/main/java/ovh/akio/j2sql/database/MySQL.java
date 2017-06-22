package ovh.akio.j2sql.database;

public class MySQL extends Connector {

    public MySQL(String host, String username, String password, String database, int port) {
        super("jdbc:mysql", host, username, password, database, port);
    }
}
