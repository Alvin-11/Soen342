package com.mycompany.app;
import java.util.ArrayList;
import java.util.HashMap;

public class TicketCatalog {
    private HashMap<Integer,Ticket> tickets;

    public TicketCatalog() {
       this.tickets = new HashMap<Integer, Ticket>();
    }

   public Ticket getTicket(int ticketID) {
        return this.tickets.get(ticketID);
    }

    public void addTicket(Ticket ticket) {
        this.tickets.put(ticket.getTicketID(), ticket);
    }

    public void deleteTicket(Ticket ticket) {
        this.tickets.remove(ticket.getTicketID());
    }

    public HashMap<Integer,Ticket> getAllTickets() {
        return this.tickets;
    }
    
    public Ticket reserveTrip(Client client, Trip trip){ // Reserves a ticket for a client for a specific trip
        for(Ticket ticket1: trip.getAllTickets()){ // Verifies that a client does not already have a ticket for the trip
            if(ticket1.getClient().getClientID().equals(client.getClientID())){
                return null;
            }
        }
        Ticket ticket = new Ticket(client, trip);
        addTicket(ticket);
        trip.addTicket(ticket);
        return ticket;
    }

    public ArrayList<Ticket> viewTrips(Client client) {
        ArrayList<Ticket> clientTickets = new ArrayList<Ticket>();

        for (Ticket ticket: this.tickets.values()) {
            if (ticket.getClient().getClientID().equals(client.getClientID())) {
                clientTickets.add(ticket);
            }
        }

        return clientTickets;
    }

    public Trip getTrip(ArrayList<Trip> trips, String ID){ // Retrieves a trip based on its ID 
        for(Trip trip: trips){
            if(trip.getTripID().equals(ID)){
                return trip;
            }
        }
        return null;
    }
    
}
