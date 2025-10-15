package com.mycompany.app;
import java.util.Random;
public class Ticket {
    Random rand = new Random();
    private final int ticketID = rand.nextInt(10000000);
    private Trip trip;
    private Client client;


    public Ticket(Client client, Trip trip) {
        this.trip = trip;
        this.client = client;

    }
    public int getTicketID() {
        return ticketID;
    }
    public Trip getTrip() {
        return trip;
    }
    public void setTrip(Trip trip) {
        this.trip = trip;
    }
    public Client getClient() {
        return client;
    }
    public void setClient(Client client) {
        this.client = client;
    }
        
}
