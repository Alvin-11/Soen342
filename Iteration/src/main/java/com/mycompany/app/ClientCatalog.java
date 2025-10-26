package com.mycompany.app;
import java.util.HashMap;

public class ClientCatalog {
    private HashMap<String,Client> clients; // Keyed by system clientID
    private HashMap<String,Client> clientsByPassportID; // Keyed by passport/state ID
    
    public ClientCatalog() {
        this.clients = new HashMap<String, Client>();
        this.clientsByPassportID = new HashMap<String, Client>();
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

    public HashMap<String,Client> getAllClients() {
        return this.clients;
    }
    
    public Client createClient(String firstName, String lastName, int age, String passportStateID){ 
        Client client = new Client(firstName, lastName, age, passportStateID);
        addClient(client);
        return client;
    }
    
    public Client createClient(String firstName, String lastName, int age){ // Creates a new client if such a client does not already exists
        Client client = new Client(firstName, lastName, age);
        addClient(client);
        return client;
    }
}
