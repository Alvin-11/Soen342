package com.mycompany.app;

import java.security.InvalidParameterException;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;

public class ConnectionCatalog {
    private CityCatalog cities;
    private ArrayList<Connection> connections;
    private HashMap<String, Connection> connectionsMap;

    public ConnectionCatalog(CityCatalog cities) {
        this.cities = cities;
        this.connections = new ArrayList<Connection>();
        this.connectionsMap = new HashMap<String, Connection>();

        // connect to the database
        try (java.sql.Connection conn = DriverManager.getConnection(Constants.DB_PATH)) {
            
            if (conn != null) {
                // create the connections table in the database if it doesn't exist
                Statement statement = conn.createStatement();
                statement.execute(
                    "CREATE TABLE IF NOT EXISTS Connections (" +
                    "routeID VARCHAR(10) PRIMARY KEY," +
                    "departureCity VARCHAR(30)," +
                    "arrivalCity VARCHAR(30)," +
                    "departureTime VARCHAR(10)," +
                    "arrivalTime VARCHAR(10)," +
                    "trainType VARCHAR(25)," +
                    "daysOfOperation VARCHAR(30)," +
                    "firstClassTicketRate FLOAT," +
                    "secondClassTicketRate FLOAT," +
                    "FOREIGN KEY(departureCity) REFERENCES Cities(cityName)," +
                    "FOREIGN KEY(arrivalCity) REFERENCES Cities(cityName)" +
                    ")"
                );

                // load the connections from the database
                ResultSet res = statement.executeQuery("SELECT * FROM Connections;");

                while (res.next()) {
                    // unpack the table row
                    String routeID = res.getString("routeID");
                    City departureCity = this.cities.getCity(res.getString("departureCity"));
                    City arrivalCity = this.cities.getCity(res.getString("arrivalCity"));
                    String departureTime = res.getString("departureTime");
                    String arrivalTime = res.getString("arrivalTime");
                    String trainType = res.getString("trainType");
                    ArrayList<String> daysOfOperation = daysOfOperationStringProcessing(res.getString("daysOfOperation"));
                    double firstClassTicketRate = res.getDouble("firstClassTicketRate");
                    double secondClassTicketRate = res.getDouble("secondClassTicketRate");

                    // create the connection
                    Connection route = new Connection(routeID, departureCity, arrivalCity, departureTime, arrivalTime, trainType, daysOfOperation, firstClassTicketRate, secondClassTicketRate);
                    
                    // add the connection to connections and connectionsMap
                    this.connections.add(route);
                    this.connectionsMap.put(routeID, route);

                    // add the connection to the incoming connections of the arrival city
                    arrivalCity.incomingConnections.add(route);

                    // add the connection to the outgoing connections of the departure city
                    departureCity.outgoingConnections.add(route);
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

    public Connection getConnection(int index) {
        return this.connections.get(index);
    }

    public Connection getConnection(String routeID) {
        return this.connectionsMap.get(routeID);
    }

    public Connection createConnection(String routeID, String departureCityName, String arrivalCityName, String departureTime, String arrivalTime,
            String trainType, String daysOfOperation, double firstClassTicketRate, double secondClassTicketRate) {

        // precondition: connection must not be in connections
        if (this.connectionsMap.get(routeID) != null) {
            throw new InvalidParameterException("ERROR: Connection " + routeID + " already exists.");
        }

        City departureCity = this.cities.getCity(departureCityName);
        City arrivalCity = this.cities.getCity(arrivalCityName);

        if (departureCity == null) {
            departureCity = this.cities.createCity(departureCityName);
        }

        if (arrivalCity == null) {
            arrivalCity = this.cities.createCity(arrivalCityName);
        }

        // create the connection
        Connection route = new Connection(routeID, departureCity, arrivalCity, departureTime, arrivalTime, trainType, daysOfOperationStringProcessing(daysOfOperation), firstClassTicketRate, secondClassTicketRate);
        
        // add the connection to connections
        this.connections.add(route);

        // add the connection to the incoming connections of the arrival city
        arrivalCity.incomingConnections.add(route);

        // add the connection to the outgoing connections of the departure city
        departureCity.outgoingConnections.add(route);

        // connect to the database
        try (java.sql.Connection conn = DriverManager.getConnection(Constants.DB_PATH)) {

            if (conn != null) {
                // add the city to the database
                PreparedStatement statement = conn.prepareStatement(
                    "INSERT INTO Connections (" +
                    "routeID," +
                    "departureCity," +
                    "arrivalCity," +
                    "departureTime," +
                    "arrivalTime," +
                    "trainType," +
                    "daysOfOperation," +
                    "firstClassTicketRate," +
                    "secondClassTicketRate" +
                    ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?);"
                );
                statement.setString(1, routeID);
                statement.setString(2, departureCityName);
                statement.setString(3, arrivalCityName);
                statement.setString(4, departureTime);
                statement.setString(5, arrivalTime);
                statement.setString(6, trainType);
                statement.setString(7, daysOfOperation);
                statement.setDouble(8, firstClassTicketRate);
                statement.setDouble(9, secondClassTicketRate);
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

        return route;
    }

    public int size() {
        return this.connections.size();
    }
    
    public ArrayList<Connection> getAllConnections() {
        return this.connections;
    }

    public static ArrayList<String> daysOfOperationStringProcessing(String daysOfOperation) {
        String[] DaysInAWeek = { "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday" };
        String[] DaysInaWeekShortened = { "Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun" };
        ArrayList<String> daysofOperation1 = new ArrayList<String>();
        if (daysOfOperation.equalsIgnoreCase("Daily")) {
            for (String day : DaysInAWeek) {
                daysofOperation1.add(day);
            }
        } else {
            String[] days = daysOfOperation.split(",");
            for (String data : days) {
                if (data.length() < 6) {
                    for (int i = 0; i < 7; i++) {
                        if (data.equalsIgnoreCase(DaysInaWeekShortened[i])) {
                            daysofOperation1.add(DaysInAWeek[i]);
                        }
                    }
                } else if (data.length() == 7) {
                    String[] dayStrings = data.split("-");
                    int startIndex = 0;
                    int endIndex = 0;
                    for (int i = 0; i < 7; i++) {
                        if (dayStrings[0].equalsIgnoreCase(DaysInaWeekShortened[i])) {
                            startIndex = i;
                        }
                        if (dayStrings[1].equalsIgnoreCase(DaysInaWeekShortened[i])) {
                            endIndex = i;
                        }
                    }
                    if (startIndex <= endIndex) {
                        for (int i = startIndex; i < endIndex + 1; i++) {
                            daysofOperation1.add(DaysInAWeek[i]);

                        }
                    } else if (endIndex < startIndex) {
                        for (int i = 0; i < 7; i++) {
                            if (i <= endIndex || i >= startIndex) {
                                daysofOperation1.add(DaysInAWeek[i]);
                            }

                        }
                    }
                }
            }
        }
        return daysofOperation1;
    }
}
