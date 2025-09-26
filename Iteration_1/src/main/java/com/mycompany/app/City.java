package com.mycompany.app;

import java.util.HashMap;

public class City {
    private final String CITY_NAME;
    private HashMap<String, Connection> DepartureCity = new HashMap<String, Connection>();
    private HashMap<String, Connection> ArrivalCity = new HashMap<String, Connection>();

    public City(String cityName) {
        this.CITY_NAME = cityName;
    }

    public String getCityName() {
        return this.CITY_NAME;
    }

    public void addDepartureCity(String Name , Connection conn) {
        this.DepartureCity.put(Name, conn);
    }

    public void addArrivalCity(String Name , Connection conn) {
        this.ArrivalCity.put(Name, conn);
    }

    public Connection getDepartureCity(String Name) {
        return this.DepartureCity.get(Name);
    }

    public Connection getArrivalCity(String Name) {
        return this.ArrivalCity.get(Name);
    }

    public HashMap<String,Connection> getAllDepartureCity() {
        return this.DepartureCity;
    }
    public HashMap<String,Connection> getAllArrivalCity() {
        return this.DepartureCity;
    }
}
