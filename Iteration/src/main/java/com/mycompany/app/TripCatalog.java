package com.mycompany.app;

import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;

public class TripCatalog {
    HashMap<Integer, Trip> trips;
    ConnectionCatalog connections;

    public TripCatalog(ConnectionCatalog connections) {
        this.trips = new HashMap<Integer, Trip>();
        this.connections = connections;

        // connect to the database
        try (java.sql.Connection conn = DriverManager.getConnection(Constants.DB_PATH)) {

            if (conn != null) {
                // create the trips table in the database if it doesn't exist
                Statement statement = conn.createStatement();
                statement.execute(
                    "CREATE TABLE IF NOT EXISTS Trips (" +
                    "tripID INT PRIMARY KEY," +
                    "departureCity VARCHAR(30)," +
                    "arrivalCity VARCHAR(30)," +
                    "departureTime INT," +
                    "initialWaitTime INT," +
                    "travelTime INT," +
                    "changeWaitTime INT," +
                    "firstClassTicketRate FLOAT," +
                    "secondClassTicketRate FLOAT," +
                    "connection1 VARCHAR(10)," +
                    "connection2 VARCHAR(10)," +
                    "connection3 VARCHAR(10)," +
                    "FOREIGN KEY(departureCity) REFERENCES Cities(cityName)," +
                    "FOREIGN KEY(arrivalCity) REFERENCES Cities(cityName)," +
                    "FOREIGN KEY(connection1) REFERENCES Connections(routeID)," +
                    "FOREIGN KEY(connection2) REFERENCES Connections(routeID)," +
                    "FOREIGN KEY(connection3) REFERENCES Connections(routeID)" +
                    ")"
                );

                // load the trips from the database
                ResultSet res = statement.executeQuery("SELECT tripID, departureTime, connection1, connection2, connection3 FROM Trips;");

                while (res.next()) {
                    // unpack the table row
                    int tripID = res.getInt("tripID");
                    int departureTime = res.getInt("departureTime");

                    String str = res.getString("connection1");
                    Connection conn1 = str == null ? null : connections.getConnection(str);
                    str = res.getString("connection2");
                    Connection conn2 = str == null ? null : connections.getConnection(str);
                    str = res.getString("connection3");
                    Connection conn3 = str == null ? null : connections.getConnection(str);

                    // create the trip
                    Trip trip;
                    if (conn1 != null && conn2 == null && conn3 == null) {
                        trip = new Trip(tripID, new Connection[]{conn1}, getDepartureDay(departureTime), getDepartureTime(departureTime));
                    }
                    else if (conn1 != null && conn2 != null && conn3 == null) {
                        trip = new Trip(tripID, new Connection[]{conn1, conn2}, getDepartureDay(departureTime), getDepartureTime(departureTime));
                    }
                    else if (conn1 != null && conn2 != null && conn3 != null) {
                        trip = new Trip(tripID, new Connection[]{conn1, conn2, conn3}, getDepartureDay(departureTime), getDepartureTime(departureTime));
                    }
                    else {
                        throw new Exception("ERROR: Invalid connections for trip: " + tripID);
                    }

                    // add the trip to trips
                    this.trips.put(tripID, trip);
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

    public Trip getTrip(int tripID) {
        return this.trips.get(tripID);
    }

    public void addTrip(Trip trip) {
        this.trips.put(trip.getTripID(), trip);
        ArrayList<Connection> connections = trip.getConnections();

        // connect to the database
        try (java.sql.Connection conn = DriverManager.getConnection(Constants.DB_PATH)) {

            if (conn != null) {
                // add the trip to the database
                PreparedStatement statement = conn.prepareStatement(
                    "INSERT INTO Trips (" +
                    "tripID," +
                    "departureCity," +
                    "arrivalCity," +
                    "departureTime," +
                    "initialWaitTime," +
                    "travelTime," +
                    "changeWaitTime," +
                    "firstClassTicketRate," +
                    "secondClassTicketRate," +
                    "connection1," +
                    "connection2," +
                    "connection3" +
                    ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);"
                );
                statement.setInt(1, trip.getTripID());
                statement.setString(2, trip.getDepartureCity().getCityName());
                statement.setString(3, trip.getArrivalCity().getCityName());
                statement.setInt(4, trip.getDesiredDepartureTime());
                statement.setInt(5, trip.getInitialWaitTime());
                statement.setInt(6, trip.getTravelTime());
                statement.setInt(7, trip.getChangeWaitTime());
                statement.setDouble(8, trip.getFirstClassTicketRate());
                statement.setDouble(9, trip.getSecondClassTicketRate());
                statement.setString(10, connections.size() > 0 ? connections.get(0).getRouteID() : null);
                statement.setString(11, connections.size() > 1 ? connections.get(1).getRouteID() : null);
                statement.setString(12, connections.size() > 2 ? connections.get(2).getRouteID() : null);
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
    }

    private static String getDepartureDay(int minutes) {
        String[] DaysInAWeek = { "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday" };
        int days = minutes / (60 * 24);

        if (days >= DaysInAWeek.length) {
            throw new IllegalArgumentException("ERROR: Minutes exceed number of days in a week.");
        }

        return DaysInAWeek[days];
    }

    private static String getDepartureTime(int minutes) {
        minutes %= (60 * 24);
        int hours = minutes / 60;
        minutes %= 60;

        return String.format("%02d:%02d", hours, minutes);
    }
}