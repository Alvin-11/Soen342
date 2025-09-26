package com.mycompany.app;
import java.util.ArrayList;
public class Connection {
    private String routeID;
    public City departureCity;
    public City arrivalCity;
    public String departureTime;
    public String arrivalTime;
    public String trainType;
    public ArrayList<String> daysOfOperation;
    public double firstClassTicketRate;
    public double secondClassTicketRate;

    public Connection(String routeID, City departureCity, City arrivalCity, String departureTime, String arrivalTime,
            String trainType, ArrayList<String> daysOfOperation, double firstClassTicketRate, double secondClassTicketRate) {
        this.routeID = routeID;
        this.departureCity = departureCity;
        this.arrivalCity = arrivalCity;
        this.departureTime = departureTime;
        this.arrivalTime = arrivalTime;
        this.trainType = trainType;
        this.daysOfOperation = daysOfOperation;
        this.firstClassTicketRate = firstClassTicketRate;
        this.secondClassTicketRate = secondClassTicketRate;
    }

    public String getRouteID() {
        return this.routeID;
    }
}