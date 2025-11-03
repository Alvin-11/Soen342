package com.mycompany.app;

public class Ticket {
    private static int idCounter = 0;

    private int ticketID;
    private Trip trip;
    private Client client;


    public Ticket(Client client, Trip trip) {
        this.ticketID = ++idCounter;
        this.trip = trip;
        this.client = client;

    }
    public int getTicketID() {
        return ticketID;
    }
    public void setTicketID(int ticketID) {
        this.ticketID = ticketID;
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
