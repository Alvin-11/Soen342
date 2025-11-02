package com.mycompany.app;

import java.security.InvalidParameterException;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;

public class CityCatalog {
    private HashMap<String, City> cities;

    public CityCatalog() {
        this.cities = new HashMap<String, City>();

        // connect to the database
        try (java.sql.Connection conn = DriverManager.getConnection(Constants.DB_PATH)) {
            
            if (conn != null) {
                // create the city table in the database if it doesn't exist
                Statement statement = conn.createStatement();
                statement.execute(
                    "CREATE TABLE IF NOT EXISTS Cities (" +
                    "cityName VARCHAR(30) PRIMARY KEY" +
                    ")"
                );

                // load the cities from the database
                ResultSet res = statement.executeQuery("SELECT cityName FROM Cities;");
                
                while (res.next()) {
                    String cityName = res.getString("cityName");
                    this.cities.put(cityName.trim().toUpperCase(), new City(cityName));
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

    public City getCity(String cityName) {
        return this.cities.get(cityName.trim().toUpperCase());
    }

    public City createCity(String cityName) {
        String mapKey = cityName.trim().toUpperCase();

        // precondition: city must not be in cities
        if (this.cities.get(mapKey) != null) {
            throw new InvalidParameterException("ERROR: City " + cityName + " already exists.");
        }

        //create the city
        City city = new City(cityName);

        // add the city to cities
        this.cities.put(mapKey, city);

        // connect to the database
        try (java.sql.Connection conn = DriverManager.getConnection(Constants.DB_PATH)) {

            if (conn != null) {
                // add the city to the database
                PreparedStatement statement = conn.prepareStatement("INSERT INTO Cities (cityName) VALUES (?);");
                statement.setString(1, cityName);
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

        return city;
    }
}