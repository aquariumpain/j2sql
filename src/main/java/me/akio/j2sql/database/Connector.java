package me.akio.j2sql.database;

import java.sql.*;

public abstract class Connector {

    private Connection connection;
    private PreparedStatement preparedStatement;

    private int index;

    private String driverString;
    private String host;
    private String username;
    private String password;
    private String database;
    private int port;

    public Connector(String driverString, String host, String username, String password, String database, int port) {
        this.driverString = driverString;
        this.host = host;
        this.username = username;
        this.password = password;
        this.database = database;
        this.port = port;
    }

    public String getDriverString() {
        return driverString;
    }

    public void setDriverString(String driverString) {
        this.driverString = driverString;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public Connection getConnection() {
        return connection;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    public boolean connect() {
        try {
            if(!this.isConnected())
                this.setConnection(DriverManager.getConnection(this.getDriverString() + "://" + this.getHost() + ":" + this.getPort() + "/" + this.getDatabase(), this.getUsername(), this.getPassword()));
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean isConnected() {
        try {
            return this.getConnection() != null && !this.getConnection().isClosed() && this.getConnection().isValid(5);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean disconnect(){
        if(!this.isConnected()) return true;
        try {
            this.getConnection().close();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public J2SQLStatement open(String sql) throws SQLException {
        if(!this.isConnected())
            throw new SQLException("[JSQL] Can't prepare statement : No connection to database established.");
        return new J2SQLStatement().open(this.getConnection().prepareStatement(sql));
    }

}
