package com.mycompany.app;
import java.util.ArrayList;

public class TicketCatalog {
    private ArrayList<Ticket> tickets;

    public TicketCatalog() {
        this.tickets = new ArrayList<Ticket>();
    }
    public void addTicket(Ticket ticket) {
        this.tickets.add(ticket);
    }
    public Ticket getTicket(int index) {
        return this.tickets.get(index);
    }
    public int size() {
        return this.tickets.size();
    }
    public ArrayList<Ticket> getAllTickets() {
        return this.tickets;
    }
    
}
