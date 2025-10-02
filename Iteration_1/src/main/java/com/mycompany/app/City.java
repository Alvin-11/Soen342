package com.mycompany.app;

import java.util.ArrayList;

public class City {
    private final String CITY_NAME;

    public ArrayList<Connection> incomingConnections;
    public ArrayList<Connection> outgoingConnections;

    public City(String cityName) {
        this.CITY_NAME = cityName;
        this.incomingConnections = new ArrayList<Connection>();
        this.outgoingConnections = new ArrayList<Connection>();
    }

    public String getCityName() {
        return this.CITY_NAME;
    }
}