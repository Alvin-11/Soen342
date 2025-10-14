package com.mycompany.app;
import java.util.ArrayList;

public class ClientCatalog {
    private ArrayList<Client> clients;
    public ClientCatalog() {
        this.clients = new ArrayList<Client>();
    }
    public void addClient(Client client) {
        this.clients.add(client);
    }
    public Client getClient(int index) {
        return this.clients.get(index);
    }
    public int size() {
        return this.clients.size();
    }
    public ArrayList<Client> getAllClients() {
        return this.clients;
    }
}
