package com.mycompany.app;
import java.util.ArrayList;
import java.util.HashMap;

public class ClientCatalog {
    private HashMap<String,Client> clients;
    public ClientCatalog() {
        this.clients = new HashMap<String, Client>();
    }
   public Client getCity(String clientID) {
        return this.clients.get(clientID);
    }

    public void addClient(Client client) {
        this.clients.put(client.getClientID(), client);
    }

    public void deleteClient(Client client) {
        this.clients.remove(client.getClientID());
    }

    public HashMap<String,Client> getAllClients() {
        return this.clients;
    }
}
