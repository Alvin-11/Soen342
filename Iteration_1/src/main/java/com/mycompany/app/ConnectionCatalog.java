package com.mycompany.app;

import java.util.ArrayList;

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
}
