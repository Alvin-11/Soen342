package com.mycompany.app;
import java.security.InvalidParameterException;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;

public class TicketCatalog {
    private HashMap<Integer,Ticket> tickets;
    private ClientCatalog clients;
    private TripCatalog trips;

    public TicketCatalog(ClientCatalog clients, TripCatalog trips) {
        this.clients = clients;
        this.trips = trips;
        this.tickets = new HashMap<Integer, Ticket>();

        try (java.sql.Connection conn = DriverManager.getConnection(Constants.DB_PATH)) {
            
            if (conn != null) {
                // create the connections table in the database if it doesn't exist
                Statement statement = conn.createStatement();
                statement.execute(
                    "CREATE TABLE IF NOT EXISTS Tickets (" +
                    "ticketID INTEGER PRIMARY KEY," +
                    "client TEXT," +
                    "trip INTEGER," +
                    "FOREIGN KEY(client) REFERENCES Clients(clientID)," +
                    "FOREIGN KEY(trip) REFERENCES Trips(tripID)" +
                    ")"
                );

                // load the connections from the database
                ResultSet res = statement.executeQuery("SELECT * FROM Tickets;");

                while (res.next()) {
                    // unpack the table row
                     int ticketID = res.getInt("ticketID");
                     Trip trip = trips.getTrip(res.getInt("trip"));
                     Client client = clients.getClient(res.getString("client"));
                
                    // create the ticket
                    Ticket ticket = new Ticket(client,trip);
                    ticket.setTicketID(ticketID);
                    this.tickets.put(ticketID, ticket);
                    trip.addTicket(ticket);
                    
                }

                statement.close();
                res.close();
            }
            else {
                throw new Exception("ERROR: Unable to connect to database.");
            }
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
        }
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

 

        // connect to the database
        try (java.sql.Connection conn = DriverManager.getConnection(Constants.DB_PATH)) {

            if (conn != null) {
                // add the city to the database
                PreparedStatement statement = conn.prepareStatement(
                    "INSERT INTO Tickets (" +
                    "ticketID," +
                    "client," +
                    "trip" +
                    ") VALUES (?, ?, ?);"
                );
                statement.setInt(1, ticket.getTicketID());
                statement.setString(2, client.getClientID());
                statement.setInt(3, trip.getTripID());
                statement.executeUpdate();
                statement.close();
            }
            else {
                throw new Exception("ERROR: Unable to connect to database.");
            }
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
        }
        
        
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

    public Trip getTrip(ArrayList<Trip> trips, int ID){ // Retrieves a trip based on its ID 
        for(Trip trip: trips){
            if(trip.getTripID()== (ID)){
                return trip;
            }
        }
        return null;
    }
    
}
