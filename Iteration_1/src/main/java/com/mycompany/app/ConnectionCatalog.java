package com.mycompany.app;

import java.util.*;

public class ConnectionCatalog {
    private ArrayList<Connection> connections;

    public ConnectionCatalog() {
        this.connections = new ArrayList<Connection>();
    }

    public void addConnection(Connection conn) {
        this.connections.add(conn);
    }

    public Connection getConnection(int index) {
        return this.connections.get(index);
    }

    public int size() {
        return this.connections.size();
    }

    public ArrayList<Connection> getAllConnections() {
        return this.connections;
    }

    public void setAllConnections(ArrayList<Connection> connection) {
        this.connections = connection;
    }
}
