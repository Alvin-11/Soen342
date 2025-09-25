package com.mycompany.app;

import java.util.*;

public class App {
    public static void main(String[] args) {
        Console console = new Console();
        System.out.println("Welcome to the Train Connection App!");
        console.addFilePath("C:/Soen342/Iteration_1/src/main/resources/eu_rail_network.csv");
        ArrayList<Connection> catalog = console.DeepCopy(console.indirectConnectionsCatalog);
        catalog = console.PrepareIndirectConnections(catalog);
        ArrayList<Connection> catalog1 = console.ReturnAllConnectionsForTrainType(catalog, "InterCity", 1);
        for (Connection conn1 : catalog1) {
            for (Connection conn : conn1.connections) {
                System.out.println("|" + conn1.getRouteID() + "|" + conn1.departureCity.getCityName() + "|"
                        + conn1.arrivalCity.getCityName()
                        + "|" + conn1.departureTime + "|" + conn1.arrivalTime + "|" + conn1.trainType
                        + "|" + conn1.daysOfOperation + "|"
                        + conn1.firstClassTicketRate
                        + "|" + conn1.secondClassTicketRate + "|" + "--->|" + conn.getRouteID() + "|"
                        + conn.departureCity.getCityName() + "|"
                        + conn.arrivalCity.getCityName()
                        + "|" + conn.departureTime + "|" + conn.arrivalTime + "|" + conn.trainType
                        + "|" + conn.daysOfOperation + "|"
                        + conn.firstClassTicketRate
                        + "|" + conn.secondClassTicketRate + "|");
            }
        }
        System.out.println("Welcome to the Train Connection App!");
        // CLI cli = new CLI();
        // cli.start();
    }
}
