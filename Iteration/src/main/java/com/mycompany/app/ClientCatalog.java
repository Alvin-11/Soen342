package com.mycompany.app;

import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;

public class ClientCatalog {
    private HashMap<String, Client> clients; // Keyed by system clientID
    private HashMap<String, Client> clientsByPassportID; // Keyed by passport/state ID

    public ClientCatalog() {
        this.clients = new HashMap<String, Client>();
        this.clientsByPassportID = new HashMap<String, Client>();

        // connect to the database and ensure table exists, then load clients
        try (java.sql.Connection conn = DriverManager.getConnection(Constants.DB_PATH)) {

            if (conn != null) {
                Statement statement = conn.createStatement();
                statement.execute(
                        "CREATE TABLE IF NOT EXISTS Clients (" +
                                "clientID VARCHAR(10) PRIMARY KEY," +
                                "firstName VARCHAR(30)," +
                                "lastName VARCHAR(30)," +
                                "age INTEGER," +
                                "passportStateID VARCHAR(30) UNIQUE" +
                                ")");

                // load clients
                ResultSet res = statement
                        .executeQuery("SELECT clientID, firstName, lastName, age, passportStateID FROM Clients;");
                int maxId = 0;
                while (res.next()) {
                    String clientID = res.getString("clientID");
                    String firstName = res.getString("firstName");
                    String lastName = res.getString("lastName");
                    int age = res.getInt("age");
                    String passportStateID = res.getString("passportStateID");

                    Client client = new Client(clientID, firstName, lastName, age, passportStateID);
                    addClient(client);
                    System.out.println(lastName);

                    // update max numeric id part
                    try {
                        if (clientID != null && clientID.length() > 1 && clientID.charAt(0) == 'C') {
                            int numeric = Integer.parseInt(clientID.substring(1));
                            if (numeric > maxId)
                                maxId = numeric;
                        }
                    } catch (NumberFormatException e) {
                        // ignore malformed IDs
                    }
                }

                // set Client.idCounter so new clients continue numbering correctly
                Client.setIdCounter(maxId);

                statement.close();
                res.close();
            } else {
                throw new Exception("ERROR: Unable to connect to database.");
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public Client getClient(String clientID) {
        return this.clients.get(clientID);
    }

    public Client getClientByPassportID(String passportStateID) {
        return this.clientsByPassportID.get(passportStateID);
    }

    public void addClient(Client client) {
        this.clients.put(client.getClientID(), client);
        if (client.getPassportStateID() != null) {
            this.clientsByPassportID.put(client.getPassportStateID(), client);
        }
    }

    public void deleteClient(Client client) {
        this.clients.remove(client.getClientID());
        if (client.getPassportStateID() != null) {
            this.clientsByPassportID.remove(client.getPassportStateID());
        }
    }

    public HashMap<String, Client> getAllClients() {
        return this.clients;
    }

    public Client createClient(String firstName, String lastName, int age, String passportStateID) {
        Client client = new Client(firstName, lastName, age, passportStateID);
        addClient(client);
        saveClientToDB(client);
        return client;
    }

    public Client createClient(String firstName, String lastName, int age) { // Creates a new client if such a client
                                                                             // does not already exists
        Client client = new Client(firstName, lastName, age);
        addClient(client);
        saveClientToDB(client);
        return client;
    }

    public void saveClientToDB(Client client) {
        try (java.sql.Connection conn = java.sql.DriverManager.getConnection(Constants.DB_PATH)) {
            if (conn != null){
                PreparedStatement stmt = conn.prepareStatement("INSERT OR REPLACE INTO Clients (clientID, firstName, lastName, age, passportStateID) VALUES (?, ?, ?, ?, ?)");

                stmt.setString(1, client.getClientID());
                stmt.setString(2, client.getFirstName());
                stmt.setString(3, client.getLastName());
                stmt.setInt(4, client.getAge());
                stmt.setString(5, client.getPassportStateID());
                stmt.executeUpdate();
                stmt.close();
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
