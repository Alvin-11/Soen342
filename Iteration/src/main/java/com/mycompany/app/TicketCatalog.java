package com.mycompany.app;
import java.util.ArrayList;
import java.util.HashMap;

public class TicketCatalog {
    private HashMap<Integer,Ticket> tickets;

    public TicketCatalog() {
       this.tickets = new HashMap<Integer, Ticket>();
    }

   public Ticket getCity(int ticketID) {
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
    
}
