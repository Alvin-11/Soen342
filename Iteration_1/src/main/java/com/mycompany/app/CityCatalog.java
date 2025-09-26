package com.mycompany.app;

import java.util.HashMap;

public class CityCatalog {
    private HashMap<String, City> cities;

    public CityCatalog() {
        this.cities = new HashMap<String, City>();
    }

    public City getCity(String cityName) {
        return this.cities.get(cityName);
    }

    public void addCity(City city) {
        this.cities.put(city.getCityName(), city);
    }

    public void deleteCity(City city) {
        this.cities.remove(city.getCityName());
    }

    public City getCitybyName(String cityName){
        return cities.get(cityName);
    }
}