package com.mycompany.app;

public class Ticket {
    private static int idCounter = 1;

    private final int ticketID;
    private Trip trip;
    private Client client;

    public Ticket(Client client, Trip trip) {
        this.ticketID = idCounter++;
        this.trip = trip;
        this.client = client;

    }

    public Ticket(int ticketID, Client client, Trip trip) {
        idCounter = Integer.max(idCounter, ticketID + 1);
        this.ticketID = ticketID;
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
