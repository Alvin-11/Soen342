package com.mycompany.app;

import java.util.*;

public class App {
    public static void main(String[] args) {
        Console console = new Console();
        System.out.println("Welcome to the Train Connection App!");
        console.addFilePath("C:/Soen342/Iteration_1/src/main/resources/eu_rail_network.csv");
        ArrayList<Connection> catalog = console.DeepCopy(console.indirectConnectionsCatalog);
        catalog = console.ReturnAllConnectionsForArrivalCity(catalog, "Warsaw");
        for (Connection conn : catalog) {
            System.out.println("|" + conn.getRouteID() + "|" + conn.departureCity.getCityName() + "|"
                    + conn.arrivalCity.getCityName()
                    + "|" + conn.departureTime + "|" + conn.arrivalTime + "|" + conn.trainType
                    + "|" + conn.daysOfOperation + "|"
                    + conn.firstClassTicketRate
                    + "|" + conn.secondClassTicketRate + "|");
        }
        System.out.println("Welcome to the Train Connection App!");
        // CLI cli = new CLI();
        // cli.start();
    }
}
