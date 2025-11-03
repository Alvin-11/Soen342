package com.mycompany.app;

import java.util.ArrayList;

public class Trip {
    private static final int MINUTES_PER_HOUR = 60;
    private static final int MINUTES_PER_DAY = MINUTES_PER_HOUR * 24;
    private static final int MINUTES_PER_WEEK = MINUTES_PER_DAY * 7;

    private static int nextTripId = 1000; // Start from 1000 for cleaner IDs

    private final int tripID;

    private ArrayList<Ticket> tickets;
    private City departureCity;
    private City arrivalCity;
    private double firstClassTicketRate;
    private double secondClassTicketRate;
    private ArrayList<Connection> connections;

    private int departureTime;   // the desired time of departure (minutes since Monday at 00:00)
    private int initialWaitTime; // the time between the desired time of departure and the departure time of the first connection (in minutes)
    private int travelTime;      // the time spent by connections (in minutes)
    private int changeWaitTime;  // the time spent in between connections (in minutes)
    // Example:
    //
    // Let the following be a trip composed of three connections
    // @@@@@@*****#####$$$$####$$$#######
    //
    // @: departureTime
    // *: initialWaitTime
    // #: travelTime
    // $: changeWaitTime

    public Trip(Connection[] conns) {
        this(conns, "Monday", "00:00");
    }

    public Trip(Connection[] conns, String departureDay, String departureTime) {
        this.tickets = new ArrayList<Ticket>();
        this.connections = new ArrayList<Connection>(conns.length);
        this.firstClassTicketRate = 0;
        this.secondClassTicketRate = 0;

        // initialize the departure and arrival cities
        if (conns.length > 0) {
            this.departureCity = conns[0].departureCity;
            this.arrivalCity = conns[conns.length - 1].arrivalCity;
        }
        else {
            this.departureCity = null;
            this.arrivalCity = null;
        }

        // verify that the departureDay is valid
        String[] DaysInAWeek = { "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday" };
        int dayIndex = -1;

        for (int i = 0; i < DaysInAWeek.length; i++) {
            if (departureDay.equals(DaysInAWeek[i])) {
                dayIndex = i;
                break;
            }
        }

        if (dayIndex == -1) {
            throw new IllegalArgumentException("The provided departure day is invalid.");
        }

        // the current time of day in minutes
        // used in conjunction with dayIndex to keep track of the day/time while parsing the connections array
        int currDayMinutes = timeToMinutes(departureTime);

        // initialize the times
        this.departureTime = (dayIndex * MINUTES_PER_DAY) + currDayMinutes;
        this.initialWaitTime = 0;
        this.travelTime = 0;
        this.changeWaitTime = 0;

        // parse the connections array
        for (int i = 0; i < conns.length; i++) {
            Connection currConn = conns[i];
            Connection prevConn = i > 0 ? conns[i - 1] : null;

            // ensure that the connections form a valid trip
            if (prevConn != null && prevConn.arrivalCity != currConn.departureCity) {
                throw new IllegalArgumentException("The provided connections do not form a valid trip.");
            }

            // find the next soonest departure time
            boolean departureFound = false;
            boolean sameDayAsArrivalDay = true;
            int connDepartureMinutes = timeToMinutes(currConn.departureTime);
            int connArrivalMinutes = timeToMinutes(currConn.arrivalTime.substring(0, 5));

            while (!departureFound) {
                if (currConn.daysOfOperation.contains(DaysInAWeek[dayIndex]) && (!sameDayAsArrivalDay || currDayMinutes <= connDepartureMinutes)) {
                    
                    // offset the wait time
                    if (this.travelTime == 0) {
                        this.initialWaitTime += connDepartureMinutes - currDayMinutes;
                    }
                    else {
                        this.changeWaitTime += connDepartureMinutes - currDayMinutes;
                    }

                    // offset the travel time
                    this.travelTime += connArrivalMinutes - connDepartureMinutes;
                    
                    if (currConn.arrivalTime.endsWith("(+1d)")) {
                        this.travelTime += MINUTES_PER_DAY;
                        dayIndex = (dayIndex + 1) % 7; // go to the next day
                    }

                    // set the currDayMinutes to the connArrivalMinutes and the departureFound to be true
                    currDayMinutes = connArrivalMinutes;
                    departureFound = true;
                }
                else {
                    sameDayAsArrivalDay = false;
                    dayIndex = (dayIndex + 1) % 7; // go to the next day

                    // offset the wait time by a day
                    if (this.travelTime == 0) {
                        this.initialWaitTime += MINUTES_PER_DAY;
                    }
                    else {
                        this.changeWaitTime += MINUTES_PER_DAY;
                    }
                }
            }

            // compute the first class ticket rate and the second class ticket rate
            this.firstClassTicketRate += currConn.firstClassTicketRate;
            this.secondClassTicketRate += currConn.secondClassTicketRate;

            this.connections.add(currConn);
        }

        // Generate simple numeric tripID
        this.tripID = nextTripId++;
    }

    public City getDepartureCity() {
        return departureCity;
    }

    public City getArrivalCity() {
        return arrivalCity;
    }

    public double getFirstClassTicketRate() {
        return firstClassTicketRate;
    }

    public double getSecondClassTicketRate() {
        return secondClassTicketRate;
    }

    public int getDesiredDepartureTime() {
        return departureTime;
    }

    public int getInitialWaitTime() {
        return initialWaitTime;
    }

    public int getTravelTime() {
        return travelTime;
    }

    public int getChangeWaitTime() {
        return changeWaitTime;
    }

    public int getRealDepartureTime() {
        return initialWaitTime + departureTime;
    }

    public int getArrivalTime() {
        return initialWaitTime + departureTime + travelTime + changeWaitTime;
    }

    public int getDurationTime() {
        return travelTime + changeWaitTime;
    }

    public int getNumberOfConnections() {
        return connections.size();
    }

    public String getDepartureDate() {
        return minutesToDate(departureTime + initialWaitTime);
    }

    public String getArrivalDate() {
        return minutesToDate(departureTime + initialWaitTime + travelTime + changeWaitTime);
    }

    public String getTripDuration() {
        return minutesToElapsedTime(travelTime + changeWaitTime);
    }

    public String getWaitTimeDuration() {
        return minutesToElapsedTime(changeWaitTime);
    }

    public ArrayList<Connection> getConnections() {
        return connections;
    }

    private int timeToMinutes(String time) {
        if (time.length() != 5 || time.charAt(2) != ':') { // make sure it is well formatted (hh:mm)
            throw new IllegalArgumentException("The provided time does not follow the format hh:mm.");
        }

        int hours = Integer.parseInt(time.substring(0, 2));
        int minutes = Integer.parseInt(time.substring(3, 5));
        
        if (hours < 0 || hours > 23 || minutes < 0 || minutes > 59) {
            throw new IllegalArgumentException("The provided departure time does not represent a valid time.");
        }

        return (MINUTES_PER_HOUR * hours) + minutes;
    }

    private String minutesToDate(int minutes) {
        String[] DaysInAWeek = { "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday" };

        int weeks = minutes / MINUTES_PER_WEEK;
        minutes %= MINUTES_PER_WEEK;
    
        int days = minutes / MINUTES_PER_DAY;
        minutes %= MINUTES_PER_DAY;
    
        int hours = minutes / MINUTES_PER_HOUR;
        minutes %= MINUTES_PER_HOUR;

        return String.format("%s %02d:%02d", DaysInAWeek[days], hours, minutes) + (weeks > 0 ? " (+" + weeks + "w)" : "");
    }

    private String minutesToElapsedTime(int minutes) {
        int days = minutes / MINUTES_PER_DAY;
        minutes %= MINUTES_PER_DAY;
    
        int hours = minutes / MINUTES_PER_HOUR;
        minutes %= MINUTES_PER_HOUR;

        return (days > 0 ? days + "d " : "") + hours + "h " + minutes + "m";
    }

    public int getTripID() {
        return tripID;
    }

    public static void resetIdCounter() {
        nextTripId = 1000;
    }

    public static int getNextTripId() {
        return nextTripId;
    }

    public ArrayList<Ticket> getAllTickets() {
        return tickets;
    }

    public void addTicket(Ticket ticket) {
        this.tickets.add(ticket);
    }

    public void removeTicket(Ticket ticket) {
        this.tickets.remove(ticket);
    }
}