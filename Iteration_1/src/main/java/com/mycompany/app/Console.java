package com.mycompany.app;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.Scanner;

import com.opencsv.CSVReader;

public class Console {
    private boolean running;

    private CityCatalog cityCatalog;
    private ConnectionCatalog connectionCatalog;
    private Search currentSearch;

    private Scanner scanner;

    public Console() {
        this.running = false;

        this.cityCatalog = new CityCatalog();
        this.connectionCatalog = new ConnectionCatalog();
        this.currentSearch = new Search();

        this.scanner = new Scanner(System.in);
    }

    // Called from main to start the console
    public void start() {
        this.running = true;
        // Run csv processing here
        ConsoleFormatter.printHeader("Welcome to the European Rail Planning System");

        while (running) {
            ConsoleFormatter.displayMenu();
            handleUserInput();
        }
    }

    // Route user choice from main menu to the proper handler class/method
    private void handleUserInput() {
        try {
            int choice = scanner.nextInt();
            switch (choice) {
                case 1:
                case 2:
                case 3:
                    // ...
                    // Invalid choice
                default:
                    ConsoleFormatter.printSeperatorLine();
                    ConsoleFormatter.printBoxedLine("Please select a valid option");
            }
            // Non int
        } catch (Exception e) {
            ConsoleFormatter.printSeperatorLine();
            ConsoleFormatter.printBoxedLine("Please enter a valid number");

            // Clear the current input
            scanner.nextLine();
        }
    }

    public void addFilePath(String filePath) {
        try (FileReader fileReader = new FileReader(filePath);
                CSVReader csvReader = new CSVReader(fileReader);) {
            String[] row;

            // ignore the header
            csvReader.readNextSilently();

            // loop through every row in the CSV file
            while ((row = csvReader.readNext()) != null) {

                // unpack the CSV row
                String routeID = row[0];
                String departureCityName = row[1];
                String arrivalCityName = row[2];
                String departureTime = row[3];
                String arrivalTime = row[4];
                String trainType = row[5];
                ArrayList<String> daysOfOperation = DaysOfOperationStringProcessing(row[6]);
                double firstClassTicketRate = Double.parseDouble(row[7]);
                double secondClassTicketRate = Double.parseDouble(row[8]);

                // check if the departure and arrival cities already exist
                City departureCity = cityCatalog.getCity(departureCityName);
                City arrivalCity = cityCatalog.getCity(arrivalCityName);

                if (departureCity == null) {
                    departureCity = new City(departureCityName);
                    cityCatalog.addCity(departureCity);
                }

                if (arrivalCity == null) {
                    arrivalCity = new City(arrivalCityName);
                    cityCatalog.addCity(arrivalCity);
                }
                // create the Connection object and add it to the catalog
                Connection conn = new Connection(routeID, departureCity, arrivalCity, departureTime, arrivalTime,
                        trainType, daysOfOperation, firstClassTicketRate, secondClassTicketRate);
                connectionCatalog.addConnection(conn);

                // add the connection to the incoming connections of the arrival city
                arrivalCity.incomingConnections.add(conn);

                // add the connection to the outgoing connections of the departure city
                departureCity.outgoingConnections.add(conn);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ArrayList<String> DaysOfOperationStringProcessing(String daysOfOperation) {
        String[] DaysInAWeek = { "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday" };
        String[] DaysInaWeekShortened = { "Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun" };
        ArrayList<String> daysofOperation1 = new ArrayList<String>();
        if (daysOfOperation.equals("Daily")) {
            for (String day : DaysInAWeek) {
                daysofOperation1.add(day);
            }
        } else {
            String[] days = daysOfOperation.split(",");
            for (String data : days) {
                if (data.length() < 6) {
                    for (int i = 0; i < 7; i++) {
                        if (data.equals(DaysInaWeekShortened[i])) {
                            daysofOperation1.add(DaysInAWeek[i]);
                        }
                    }
                } else if (data.length() == 7) {
                    String[] dayStrings = data.split("-");
                    int startIndex = 0;
                    int endIndex = 0;
                    for (int i = 0; i < 7; i++) {
                        if (dayStrings[0].equals(DaysInaWeekShortened[i])) {
                            startIndex = i;
                        }
                        if (dayStrings[1].equals(DaysInaWeekShortened[i])) {
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
    
    // This method essentials loops through the cities and their connections to find all possible trips from 0 stop t0 2 stop that match the search criteria for departure city and arrival city
    public ArrayList<Trip> filterbyDepartureCityAndArrivalCity(String DepartureCity, String ArrivalCity, String departureDay, String departureTime){
        ArrayList<Trip> baseTrips = new ArrayList<Trip>();
        City departureCity = cityCatalog.getCity(DepartureCity);
        City arrivalCity = cityCatalog.getCity(ArrivalCity);
        
        if(departureCity ==null | arrivalCity==null){
            throw new IllegalArgumentException("One or both of the specified cities do not exist in the catalog.");
        }
        for(Connection conn : departureCity.outgoingConnections){
            Connection[] connections = new Connection[1];
            connections[0] = conn;
            if(conn.arrivalCity.getCityName().equals(ArrivalCity)){ // Checking that the arrival city of the connection is the same for a direct connection
                Trip trip;
                if(departureDay.length()>0 || departureTime.length()>0){
                   trip = new Trip(connections,departureDay,departureTime);}
                else{
                    trip = new Trip(connections);
                }
                baseTrips.add(trip);
            }
            City departureCity1 = cityCatalog.getCity(conn.arrivalCity.getCityName());
            for(Connection conn2: departureCity1.outgoingConnections){
                Connection[] connection2 = new Connection[2];
                connection2[0] = conn;
                connection2[1] = conn2;
            if(conn2.arrivalCity.getCityName().equals(ArrivalCity)){ // Checking that the arrival city of the connection is the same for a 1-stop Indirect connection
                Trip trip;
                if(departureDay.length()>0 || departureTime.length()>0){
                   trip = new Trip(connection2,departureDay,departureTime);}
                else{
                    trip = new Trip(connection2);
                }
                baseTrips.add(trip);
            }

             City departureCity2 = cityCatalog.getCity(conn2.arrivalCity.getCityName());
            for(Connection conn3: departureCity2.outgoingConnections){
                Connection[] connection3 = new Connection[3];
                connection3[0] = conn;
                connection3[1] = conn2;
                connection3[2] = conn3;
               if(conn3.arrivalCity.getCityName().equals(ArrivalCity)){  // Checking that the arrival city of the connection is the same for a 2-stop Indirect connection
                Trip trip;
                if(departureDay.length()>0 || departureTime.length()>0){
                   trip = new Trip(connection3,departureDay,departureTime);}
                else{
                    trip = new Trip(connection3);
                }
                baseTrips.add(trip);
            }
            }
            }
        }
        return baseTrips;
    }

    public ArrayList<Trip> filterbyTrainType(ArrayList<Trip> trip, String trainType ){ // This method filters trips based on the train type provided by the user
        ArrayList<Trip> filteredTrips = new ArrayList<Trip>();
        for(Trip trip1: trip){
            boolean valid = true;
            for(Connection conn: trip1.getConnections()){
                if(!conn.trainType.equals(trainType)){
                    valid = false;
                    break;
                }
            }
            if(valid){
                filteredTrips.add(trip1);
            }
        }
        
        return filteredTrips;
    }

    public ArrayList<Trip> filterbyFirstClassTicketRate(ArrayList<Trip> trip, int upper, int lower ){ // This method filter trips based on the first class ticket rate provided by the user
        ArrayList<Trip> filteredTrips = new ArrayList<Trip>();
        for(Trip trip1: trip){
            if(trip1.getFirstClassTicketRate()<=upper & trip1.getFirstClassTicketRate()>= lower){
                filteredTrips.add(trip1);
            }
        }
        
        return filteredTrips;
    }

    public ArrayList<Trip> filterbySecondClassTicketRate(ArrayList<Trip> trip, int upper, int lower ){ // This method filter trips based on the second class ticket rate provided by the user
        ArrayList<Trip> filteredTrips = new ArrayList<Trip>();
        for(Trip trip1: trip){
            if(trip1.getSecondClassTicketRate()<=upper & trip1.getSecondClassTicketRate()>= lower){
                filteredTrips.add(trip1);
            }
        }
        
        return filteredTrips;
    }

    public ArrayList<Trip> filterbyDaysOfOperation(ArrayList<Trip> trip, String daysOfOperation ){ // This method filter trips based on the days of operation provided by the 
        ArrayList<Trip> filteredTrips = new ArrayList<Trip>();
        ArrayList<String> daysOfOperationList = DaysOfOperationStringProcessing(daysOfOperation);
        for(Trip trip1: trip){
            boolean valid = true;
             for(Connection conn: trip1.getConnections()){ // Goes through all the connections (1 to 3 connections per trip)
                for(String day: daysOfOperationList ){ //  (1 day min to 7 days max) Goes through all the days of operatiom provided by the user and checks if the connection contains all of them in their days of operation
                    if(!conn.daysOfOperation.contains(day)){
                        valid = false;
                        break;
                    }
                }
                if(valid==false){break;}
             }
                if(valid){
                    filteredTrips.add(trip1);
                }
        }
        return filteredTrips;
    }

    // Helper class to contain all methods related to printing to the ✨console✨
    private class ConsoleFormatter {
        private static final int CONSOLE_WIDTH = 80;
        private static final String BORDER_CHAR = "█";
        private static final String PADDING_CHAR = " ";

        private ConsoleFormatter() {
        }

        private static void displayMenu() {
            printSeperatorLine();
            printCenteredLine("Search Filters");
            printSeperatorLine();
            printBoxedLine("1. Departure City");
            printBoxedLine("2. Arrival City");
            printBoxedLine("3. Departure Time");
            printBoxedLine("4. Arrival Time");
            printBoxedLine("5. Train Type");
            printBoxedLine("6. Days of Operation");
            printBoxedLine("7. Train Class Tier");
            printSeperatorLine();
            printPrompt("Enter your selection: ");
        }

        private static void printPrompt(String prompt) {
            System.out.print(BORDER_CHAR + " " + prompt);
        }

        private static void printHeader(String title) {
            printBorderLine();
            printCenteredLine(title);
            printBorderLine();
        }

        private static void printBorderLine() {
            System.out.println(BORDER_CHAR.repeat(CONSOLE_WIDTH));
        }

        private static void printCenteredLine(String text) {
            int padding = (CONSOLE_WIDTH - text.length() - 2) / 2;
            String leftPadding = PADDING_CHAR.repeat(padding);
            String rightPadding = PADDING_CHAR.repeat(CONSOLE_WIDTH - text.length() - padding - 2);
            System.out.println(BORDER_CHAR + leftPadding + text + rightPadding + BORDER_CHAR);
        }

        private static void printBoxedLine(String text) {
            int contentWidth = CONSOLE_WIDTH - 4;
            if (text.length() > contentWidth) {
                text = text.substring(0, contentWidth - 3) + "...";
            }

            String padding = PADDING_CHAR.repeat(contentWidth - text.length());
            System.out.println(BORDER_CHAR + " " + text + padding + " " + BORDER_CHAR);
        }

        private static void printSeperatorLine() {
            System.out.println(BORDER_CHAR + "-".repeat(CONSOLE_WIDTH - 2) + BORDER_CHAR);
        }

        private static void clearConsole(){
            try {
                // Windows implementation
                if (System.getProperty("os.name").contains("Windows")){
                    new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
                } else{
                    // MacOs and Linux
                    System.out.print("\033[2J\033[H");
                    System.out.flush();
                }
            } catch (Exception e) {
                // Fallback
                for (int i = 0; i<50 ;i++) System.out.println();
            }
        }
    }
}
