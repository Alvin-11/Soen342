package com.mycompany.app;

import java.util.HashMap;

public class TripCatalog {
    HashMap<String, Trip> trips;

    public TripCatalog() {
        this.trips = new HashMap<String, Trip>();
    }

    public Trip getTrip(String tripID) {
        return this.trips.get(tripID);
    }

    public void addTrip(Trip trip) {
        this.trips.put(trip.getTripID(), trip);
    }

    public void deleteTrip(Trip trip) {
        this.trips.remove(trip.getTripID());
    }
}
