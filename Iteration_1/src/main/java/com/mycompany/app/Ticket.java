package com.mycompany.app;

public class Ticket {
    private String ticketID;
    private Trip trip;

    public Ticket(String ticketID, Trip trip) {
        this.ticketID = ticketID;
        this.trip = trip;
    }
    public String getTicketID() {
        return ticketID;
    }
    public Trip getTrip() {
        return trip;
    }
    public void setTrip(Trip trip) {
        this.trip = trip;
    }
    public void setTicketID(String ticketID) {
        this.ticketID = ticketID;
    }
        
}
