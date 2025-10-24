package com.mycompany.app;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Scanner;

import com.opencsv.CSVReader;

public class Console {
    private boolean running;

    private CityCatalog cityCatalog;
    private ConnectionCatalog connectionCatalog;
    private ClientCatalog clientCatalog;
    private TicketCatalog ticketCatalog;
    private TripCatalog tripCatalog;
    private Search currentSearch;

    private Scanner scanner;

    public Console() {
        this.running = false;

        this.cityCatalog = new CityCatalog();
        this.connectionCatalog = new ConnectionCatalog();
        this.clientCatalog = new ClientCatalog();
        this.ticketCatalog = new TicketCatalog();
        this.tripCatalog = new TripCatalog();
        this.currentSearch = new Search();

        this.scanner = new Scanner(System.in);
    }

    // Called from main to start the console
    public void start() {
        this.running = true;

        // Run csv processing here
        addFilePath("Iteration/src/main/resources/eu_rail_network.csv");

        ConsoleFormatter.clearConsole();
        ConsoleFormatter.printHeader("Welcome to the European Rail Planning System");

        while (running) {
            ConsoleFormatter.displayMenu(currentSearch);
            handleUserInput();
        }
    }

    // Route user choice from main menu to the proper handler class/method
    private void handleUserInput() {
        try {
            String choice = scanner.nextLine().trim().toLowerCase();

            switch (choice) {
                case "1":
                    editDepartureCity();
                    break;
                case "2":
                    editArrivalCity();
                    break;
                case "3":
                    editDepartureTime();
                    break;
                case "4":
                    editArrivalTime();
                    break;
                case "5":
                    editTrainType();
                    break;
                case "6":
                    editDaysOfOperation();
                    break;
                case "7":
                    editSeatingClass();
                    break;
                case "8":
                    editPriceOptions();
                    break;
                case "9":
                    editSortingOptions();
                    break;

                case "0":
                    exitConsole();
                    break;
                case "r":
                    resetSearch();
                    break;
                case "s":
                case "search":
                    checkValuesForRunSearch();
                    break;
                // Invalid choice
                default:
                    ConsoleFormatter.printSeperatorLine();
                    ConsoleFormatter.printBoxedLine("Please select a valid option");
                    ConsoleFormatter.printPrompt("Press Enter to continue...");
                    scanner.nextLine();
            }
        } catch (Exception e) {
            ConsoleFormatter.printSeperatorLine();
            ConsoleFormatter.printBoxedLine("An error occurred. Please try again.");
            ConsoleFormatter.printPrompt("Press Enter to continue...");
            scanner.nextLine();
        }
    }

     public void checkValuesForRunSearch(){
        ConsoleFormatter.clearConsole();
        boolean validSearch=true;
        String errorMessage="";
        
        if(cityCatalog.getCity(this.currentSearch.getDepartureCity())==null & cityCatalog.getCity(this.currentSearch.getArrivalCity())==null){
            validSearch=false;
            errorMessage="Both departure and arrival cities do not exist in the catalog.\n";
        }
        else if (cityCatalog.getCity(this.currentSearch.getDepartureCity())==null ){
            validSearch=false;
            errorMessage="Departure city does not exist in the catalog. \n";
        }
        else if(cityCatalog.getCity(this.currentSearch.getArrivalCity())==null){
            validSearch=false;
            errorMessage="Arrival city does not exist in the catalog.\n";
        }

         if (this.currentSearch.getTrainType() != "") {
            boolean trainTypeExists=false;
            for(Connection conn: connectionCatalog.getAllConnections()){
                if(conn.trainType.equalsIgnoreCase(this.currentSearch.getTrainType())){
                    trainTypeExists=true;
                    break;
                }
            }
            if(!trainTypeExists){
                validSearch=false;
                errorMessage="Train type does not exist in the catalog.\n";
            }
         }

         if(this.currentSearch.getMinCost() != null){
         if(this.currentSearch.getMinCost() <0){
            validSearch=false;
            errorMessage="Minimum cost cannot be negative.\n";
         }}
         if(this.currentSearch.getMaxCost() != null){
         if(this.currentSearch.getMaxCost() <0){
            validSearch=false;
            errorMessage="Maximum cost cannot be negative.\n";
         }}
          if(this.currentSearch.getMinCost() != null & this.currentSearch.getMaxCost() != null){
         if(this.currentSearch.getMaxCost() < this.currentSearch.getMinCost()){
            validSearch=false;
            errorMessage="Maximum cost cannot be less than the Minimum cost.\n";
         }}

        if(!validSearch)
        {
            ConsoleFormatter.printSeperatorLine();
            ConsoleFormatter.printBoxedLine(errorMessage);
            ConsoleFormatter.printPrompt("Press Enter to continue...");
            scanner.nextLine();
        }
        else{
            runSearch();
        }
    }

    public void runSearch() {
        ConsoleFormatter.clearConsole();
        ConsoleFormatter.printHeader("Search Results");

        //get all the base trips
        ArrayList<Trip> trips = filterbyDepartureCityAndArrivalCity(
            this.currentSearch.getDepartureCity(),
            this.currentSearch.getArrivalCity(),
            this.currentSearch.getDepartureDay(),
            this.currentSearch.getDepartureTime()
        );

        // assume that if a departure day is provided then a departure time is also be provided and vice-versa
        // more permissible logic can be implemented later
        if (this.currentSearch.getDepartureDay() != "" || this.currentSearch.getDepartureTime() != "") {
            trips = filterbyDepartureTime(trips);
        }

        // assume that if an arrival day is provided then an arrival time is also be provided and vice-versa
        // more permissible logic can be implemented later
        if (this.currentSearch.getArrivalDay() != "" || this.currentSearch.getArrivalTime() != "") {
            trips = filterbyArrivalTime(trips, this.currentSearch.getArrivalDay(), this.currentSearch.getArrivalTime());
        }

        if (this.currentSearch.getTrainType() != "") {
            trips = filterbyTrainType(trips, this.currentSearch.getTrainType());
        }

        if (this.currentSearch.getDaysOfOperation() != "") {
            trips = filterbyDaysOfOperation(trips, this.currentSearch.getDaysOfOperation());
        }

        // assume that if a price filter is provided then a seating class is provided
        String seatingClass = this.currentSearch.getSeatingClass();
        Double minCost = this.currentSearch.getMinCost();
        Double maxCost = this.currentSearch.getMaxCost();

        if (minCost == null) {
            minCost = Double.MIN_VALUE;
        }

        if (maxCost == null) {
            maxCost = Double.MAX_VALUE;
        }

        if (seatingClass.equals("First Class")) {
            trips = filterbyFirstClassTicketRate(trips, maxCost, minCost);
        }
        else if (seatingClass.equals("Second Class")) {
            trips = filterbySecondClassTicketRate(trips, maxCost, minCost);
        }

        // sort the trips
        sortTrips(trips, this.currentSearch.getSortBy(), this.currentSearch.getOrder(), this.currentSearch.getSeatingClass());

        // print the trip table
        ConsoleFormatter.printTripTableHeader();
        for (Trip t : trips) {
            ConsoleFormatter.printTrip(t);
        }

        System.out.println();
        ConsoleFormatter.printPrompt("Press Enter to return to main menu...");
        scanner.nextLine();
        ConsoleFormatter.clearConsole();
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
        if (daysOfOperation.equalsIgnoreCase("Daily")) {
            for (String day : DaysInAWeek) {
                daysofOperation1.add(day);
            }
        } else {
            String[] days = daysOfOperation.split(",");
            for (String data : days) {
                if (data.length() < 6) {
                    for (int i = 0; i < 7; i++) {
                        if (data.equalsIgnoreCase(DaysInaWeekShortened[i])) {
                            daysofOperation1.add(DaysInAWeek[i]);
                        }
                    }
                } else if (data.length() == 7) {
                    String[] dayStrings = data.split("-");
                    int startIndex = 0;
                    int endIndex = 0;
                    for (int i = 0; i < 7; i++) {
                        if (dayStrings[0].equalsIgnoreCase(DaysInaWeekShortened[i])) {
                            startIndex = i;
                        }
                        if (dayStrings[1].equalsIgnoreCase(DaysInaWeekShortened[i])) {
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

    // This method essentials loops through the cities and their connections to find
    // all possible trips from 0 stop t0 2 stop that match the search criteria for
    // departure city and arrival city
    public ArrayList<Trip> filterbyDepartureCityAndArrivalCity(String departureCityName, String arrivalCityName,
            String departureDay, String departureTime) {

        ArrayList<Trip> baseTrips = new ArrayList<Trip>();
        City departureCity = cityCatalog.getCity(departureCityName);
        City arrivalCity = cityCatalog.getCity(arrivalCityName);

        if (departureCity == null || arrivalCity == null) {
            throw new IllegalArgumentException("One or both of the specified cities do not exist in the catalog.");
        }

        if (departureDay == null || departureDay.trim().isEmpty()) {
            departureDay = "Monday";
        }

        if (departureTime == null || departureTime.trim().isEmpty()) {
            departureTime = "00:00";
        }

        for (Connection conn1 : departureCity.outgoingConnections) {
            if (conn1.arrivalCity == arrivalCity) { // Checking that the arrival city of the
                                                   // connection is the same for a direct connection
                Trip trip = tripCatalog.getTrip(Trip.generateTripID(new Connection[]{conn1}, departureDay, departureTime));

                if(trip == null){
                    trip = new Trip(new Connection[]{conn1}, departureDay, departureTime);
                    tripCatalog.addTrip(trip);
                }

                baseTrips.add(trip);
            }

            for (Connection conn2 : conn1.arrivalCity.outgoingConnections) {
                if (conn2.arrivalCity == arrivalCity) { // Checking that the arrival city of the
                                                        // connection is the same for a 1-stop
                                                        // Indirect connection
                    Trip trip = tripCatalog.getTrip(Trip.generateTripID(new Connection[]{conn1, conn2}, departureDay, departureTime));

                    if(trip == null){
                        trip = new Trip(new Connection[]{conn1, conn2}, departureDay, departureTime);
                        tripCatalog.addTrip(trip);
                    }

                    baseTrips.add(trip);
                }

                for (Connection conn3 : conn2.arrivalCity.outgoingConnections) {
                    if (conn3.arrivalCity == arrivalCity) { // Checking that the arrival city of the
                                                            // connection is the same for a 2-stop
                                                            // Indirect connection
                        Trip trip = tripCatalog.getTrip(Trip.generateTripID(new Connection[]{conn1, conn2, conn3}, departureDay, departureTime));

                        if(trip == null){
                            trip = new Trip(new Connection[]{conn1, conn2, conn3}, departureDay, departureTime);
                            tripCatalog.addTrip(trip);
                        }

                        baseTrips.add(trip);
                    }
                }
            }
        }

        return baseTrips;
    }

    public ArrayList<Trip> filterbyTrainType(ArrayList<Trip> trip, String trainType) { // This method filters trips
                                                                                       // based on the train type
                                                                                       // provided by the user
        ArrayList<Trip> filteredTrips = new ArrayList<Trip>();
        for (Trip trip1 : trip) {
            boolean valid = true;
            for (Connection conn : trip1.getConnections()) {
                if (!conn.trainType.equals(trainType)) {
                    valid = false;
                    break;
                }
            }
            if (valid) {
                filteredTrips.add(trip1);
            }
        }

        return filteredTrips;
    }

    public ArrayList<Trip> filterbyFirstClassTicketRate(ArrayList<Trip> trip, double upper, double lower) { // This method
                                                                                                            // filter trips
                                                                                                            // based on the
                                                                                                            // first class
                                                                                                            // ticket rate
                                                                                                            // provided by the
                                                                                                            // user
        ArrayList<Trip> filteredTrips = new ArrayList<Trip>();
        for (Trip trip1 : trip) {
            if (trip1.getFirstClassTicketRate() <= upper & trip1.getFirstClassTicketRate() >= lower) {
                filteredTrips.add(trip1);
            }
        }

        return filteredTrips;
    }

    public ArrayList<Trip> filterbySecondClassTicketRate(ArrayList<Trip> trip, double upper, double lower) { // This method
                                                                                                             // filter trips
                                                                                                             // based on the
                                                                                                             // second class
                                                                                                             // ticket rate
                                                                                                             // provided by
                                                                                                             // the user
        ArrayList<Trip> filteredTrips = new ArrayList<Trip>();
        for (Trip trip1 : trip) {
            if (trip1.getSecondClassTicketRate() <= upper & trip1.getSecondClassTicketRate() >= lower) {
                filteredTrips.add(trip1);
            }
        }

        return filteredTrips;
    }

    public ArrayList<Trip> filterbyDaysOfOperation(ArrayList<Trip> trip, String daysOfOperation) { // This method filter
                                                                                                   // trips based on the
                                                                                                   // days of operation
                                                                                                   // provided by the
        ArrayList<Trip> filteredTrips = new ArrayList<Trip>();
        ArrayList<String> daysOfOperationList = DaysOfOperationStringProcessing(daysOfOperation);
        for (Trip trip1 : trip) {
            boolean valid = true;
            for (Connection conn : trip1.getConnections()) { // Goes through all the connections (1 to 3 connections per
                                                             // trip)
                for (String day : daysOfOperationList) { // (1 day min to 7 days max) Goes through all the days of
                                                         // operatiom provided by the user and checks if the connection
                                                         // contains all of them in their days of operation
                    if (!conn.daysOfOperation.contains(day)) {
                        valid = false;
                        break;
                    }
                }
                if (valid == false) {
                    break;
                }
            }
            if (valid) {
                filteredTrips.add(trip1);
            }
        }
        return filteredTrips;
    }

    public ArrayList<Trip> filterbyDepartureTime(ArrayList<Trip> trip) { // This method checks if the departure time and
                                                                         // day of the first connection matches the user
        ArrayList<Trip> filteredTrips = new ArrayList<Trip>();
        for (Trip trip1 : trip) {
            if (trip1.getInitialWaitTime() <= 0) {
                filteredTrips.add(trip1);
            }
        }
        return filteredTrips;
    }

    public ArrayList<Trip> filterbyArrivalTime(ArrayList<Trip> trip, String ArrivalDay, String ArrivalTime) { // This
                                                                                                              // method
                                                                                                              // checks
                                                                                                              // if the
                                                                                                              // arrival
                                                                                                              // time
                                                                                                              // and day
                                                                                                              // of the
                                                                                                              // last
                                                                                                              // connection
                                                                                                              // matches
                                                                                                              // the
                                                                                                              // user
        ArrayList<Trip> filteredTrips = new ArrayList<Trip>();
        for (Trip trip1 : trip) {
            String arrivalDate = trip1.getArrivalDate().toLowerCase();
            if (arrivalDate.contains(ArrivalDay.toLowerCase()) && arrivalDate.contains(ArrivalTime.toLowerCase())) {
                filteredTrips.add(trip1);
            }
        }
        return filteredTrips;
    }

    public void sortTrips(ArrayList<Trip> trip, String sortBy, String order, String seatingClass) {
        if (sortBy.equals("Departure Time")) {
            if (order.equals("Ascending")) {
                trip.sort(new Comparator<Trip>() {
                    public int compare(Trip t1, Trip t2) {
                        return t1.getRealDepartureTime() - t2.getRealDepartureTime();
                    }
                });
            }
            else if (order.equals("Descending")) {
                trip.sort(new Comparator<Trip>() {
                    public int compare(Trip t1, Trip t2) {
                        return t2.getRealDepartureTime() - t1.getRealDepartureTime();
                    }
                });
            }
        }
        else if (sortBy.equals("Arrival Time")) {
            if (order.equals("Ascending")) {
                trip.sort(new Comparator<Trip>() {
                    public int compare(Trip t1, Trip t2) {
                        return t1.getArrivalTime() - t2.getArrivalTime();
                    }
                });
            }
            else if (order.equals("Descending")) {
                trip.sort(new Comparator<Trip>() {
                    public int compare(Trip t1, Trip t2) {
                        return t2.getArrivalTime() - t1.getArrivalTime();
                    }
                });
            }
        }
        else if (sortBy.equals("Price")) {
            if (seatingClass.equals("First Class")) {
                if (order.equals("Ascending")) {
                    trip.sort(new Comparator<Trip>() {
                        public int compare(Trip t1, Trip t2) {
                            return (int) (t1.getFirstClassTicketRate() * 100 - t2.getFirstClassTicketRate() * 100);
                        }
                    });
                }
                else if (order.equals("Descending")) {
                    trip.sort(new Comparator<Trip>() {
                        public int compare(Trip t1, Trip t2) {
                            return (int) (t2.getFirstClassTicketRate() * 100 - t1.getFirstClassTicketRate() * 100);
                        }
                    });
                }
            }
            else if (seatingClass.equals("Second Class")) {
                if (order.equals("Ascending")) {
                    trip.sort(new Comparator<Trip>() {
                        public int compare(Trip t1, Trip t2) {
                            return (int) (t1.getSecondClassTicketRate() * 100 - t2.getSecondClassTicketRate() * 100);
                        }
                    });
                }
                else if (order.equals("Descending")) {
                    trip.sort(new Comparator<Trip>() {
                        public int compare(Trip t1, Trip t2) {
                            return (int) (t2.getSecondClassTicketRate() * 100 - t1.getSecondClassTicketRate() * 100);
                        }
                    });
                }
            }
        }
        else if (sortBy.equals("Duration")) {
            if (order.equals("Ascending")) {
                trip.sort(new Comparator<Trip>() {
                    public int compare(Trip t1, Trip t2) {
                        return t1.getDurationTime() - t2.getDurationTime();
                    }
                });
            }
            else if (order.equals("Descending")) {
                trip.sort(new Comparator<Trip>() {
                    public int compare(Trip t1, Trip t2) {
                        return t2.getDurationTime() - t1.getDurationTime();
                    }
                });
            }
        }
    }

    public ArrayList<Ticket> viewTrips(String lastName, String clientID) {
        Client client = this.clientCatalog.getClient(clientID);

        if (client == null) {
            throw new IllegalArgumentException("Client with the provided ID does not exist.");
        }
        else if (!client.getLastName().equalsIgnoreCase(lastName)) {
            throw new IllegalArgumentException("Client last name does not match the provided client ID.");
        }

        return this.ticketCatalog.viewTrips(client);
    }

    private void editDepartureCity() {
        ConsoleFormatter.clearConsole();
        ConsoleFormatter.printCenteredLine("Current departure city: " + this.currentSearch.getDepartureCity());
        ConsoleFormatter.printSeperatorLine();
        ConsoleFormatter.printPrompt("Enter new departure city (or press Enter to skip): ");

        try {
            String newDepartureCity = scanner.nextLine().trim();
            this.currentSearch.setDepartureCity(newDepartureCity);
        } catch (Exception e) {
            System.err.println(e);
        }

        ConsoleFormatter.clearConsole();
    }

    private void editArrivalCity() {
        ConsoleFormatter.clearConsole();
        ConsoleFormatter.printCenteredLine("Current arrival city: " + this.currentSearch.getArrivalCity());
        ConsoleFormatter.printSeperatorLine();
        ConsoleFormatter.printPrompt("Enter new arrival city (or press Enter to skip): ");

        try {
            String newArrivalCity = scanner.nextLine().trim();
            this.currentSearch.setArrivalCity(newArrivalCity);
        } catch (Exception e) {
            System.err.println(e);
        }

        ConsoleFormatter.clearConsole();
    }

    private void editArrivalTime() {
        ConsoleFormatter.clearConsole();
        ConsoleFormatter.printCenteredLine("Current arrival day: " +
                (currentSearch.getArrivalDay().isEmpty() ? "Not set" : currentSearch.getArrivalDay()));
        ConsoleFormatter.printCenteredLine("Current arrival time: " +
                (currentSearch.getArrivalTime().isEmpty() ? "Not set" : currentSearch.getArrivalTime()));
        ConsoleFormatter.printSeperatorLine();

        // Get arrival day
        ConsoleFormatter.printBoxedLine("Valid day formats: Monday, Tuesday, Wed, Thu, etc.");
        ConsoleFormatter.printPrompt("Enter arrival day (or press Enter to skip): ");

        String newDay = scanner.nextLine().trim();
        if (!newDay.isEmpty()) {
            if (isValidSingleDay(newDay)) {
                currentSearch.setArrivalDay(newDay);
            } else {
                ConsoleFormatter.printBoxedLine("Invalid day format. Please use a valid weekday name.");
                ConsoleFormatter.printPrompt("Press Enter to continue...");
                scanner.nextLine();
                ConsoleFormatter.clearConsole();
                return;
            }
        }

        // Get arrival time
        ConsoleFormatter.printSeperatorLine();
        ConsoleFormatter.printBoxedLine("Time format: HH:MM (24-hour, e.g., 09:30, 14:45)");
        ConsoleFormatter.printPrompt("Enter arrival time (or press Enter to skip): ");

        String newTime = scanner.nextLine().trim();
        if (!newTime.isEmpty()) {
            if (isValidTimeFormat(newTime)) {
                currentSearch.setArrivalTime(newTime);
            } else {
                ConsoleFormatter.printBoxedLine("Invalid time format. Please use HH:MM format.");
                ConsoleFormatter.printPrompt("Press Enter to continue...");
                scanner.nextLine();
            }
        }

        ConsoleFormatter.clearConsole();
    }

    private void editDepartureTime() {
        ConsoleFormatter.clearConsole();
        ConsoleFormatter.printCenteredLine("Current departure day: " +
                (currentSearch.getDepartureDay().isEmpty() ? "Not set" : currentSearch.getDepartureDay()));
        ConsoleFormatter.printCenteredLine("Current departure time: " +
                (currentSearch.getDepartureTime().isEmpty() ? "Not set" : currentSearch.getDepartureTime()));
        ConsoleFormatter.printSeperatorLine();

        // Get departure day
        ConsoleFormatter.printBoxedLine("Valid day formats: Monday, Tuesday, Wed, Thu, etc.");
        ConsoleFormatter.printPrompt("Enter departure day (or press Enter to skip): ");

        String newDay = scanner.nextLine().trim();
        if (!newDay.isEmpty()) {
            if (isValidSingleDay(newDay)) {
                currentSearch.setDepartureDay(newDay);
            } else {
                ConsoleFormatter.printBoxedLine("Invalid day format. Please use a valid weekday name.");
                ConsoleFormatter.printPrompt("Press Enter to continue...");
                scanner.nextLine();
                ConsoleFormatter.clearConsole();
                return;
            }
        }

        // Get departure time
        ConsoleFormatter.printSeperatorLine();
        ConsoleFormatter.printBoxedLine("Time format: HH:MM (24-hour, e.g., 09:30, 14:45)");
        ConsoleFormatter.printPrompt("Enter departure time (or press Enter to skip): ");

        String newTime = scanner.nextLine().trim();
        if (!newTime.isEmpty()) {
            if (isValidTimeFormat(newTime)) {
                currentSearch.setDepartureTime(newTime);
            } else {
                ConsoleFormatter.printBoxedLine("Invalid time format. Please use HH:MM format.");
                ConsoleFormatter.printPrompt("Press Enter to continue...");
                scanner.nextLine();
            }
        }

        ConsoleFormatter.clearConsole();
    }

    private void editTrainType() {
        ConsoleFormatter.clearConsole();
        ConsoleFormatter.printCenteredLine("Current train type: " + this.currentSearch.getTrainType());
        ConsoleFormatter.printSeperatorLine();
        ConsoleFormatter.printPrompt("Enter new train type (or press Enter to skip): ");

        try {
            String newTrainType = scanner.nextLine().trim();
            this.currentSearch.setTrainType(newTrainType);
        } catch (Exception e) {
            System.err.println(e);
        }

        ConsoleFormatter.clearConsole();
    }

    private void editDaysOfOperation() {
        ConsoleFormatter.clearConsole();
        ConsoleFormatter.printCenteredLine("Current days of operation: " + this.currentSearch.getDaysOfOperation());
        ConsoleFormatter.printSeperatorLine();
        ConsoleFormatter.printBoxedLine("Valid formats:");
        ConsoleFormatter.printBoxedLine("- Daily");
        ConsoleFormatter.printBoxedLine("- Single range: Mon-Fri, Saturday-Sunday");
        ConsoleFormatter.printBoxedLine("- Comma-separated: Mon,Wed,Fri or Monday,Wednesday,Friday");
        ConsoleFormatter.printSeperatorLine();
        ConsoleFormatter.printPrompt("Enter days of operation (or press Enter to skip): ");

        try {
            String newDaysOfOperation = scanner.nextLine().trim();

            if (isValidDaysOfOperationFormat(newDaysOfOperation)) {
                this.currentSearch.setDaysOfOperation(newDaysOfOperation);
            } else {
                ConsoleFormatter.printBoxedLine(
                        "Invalid format. Please use Daily, a single range (Mon-Fri), or comma-separated days (Mon,Wed,Fri).");
                ConsoleFormatter.printPrompt("Press Enter to continue...");
                scanner.nextLine();
            }
        } catch (Exception e) {
            ConsoleFormatter.printBoxedLine("An error occurred while processing your input.");
            ConsoleFormatter.printPrompt("Press Enter to continue...");
            scanner.nextLine();
        }

        ConsoleFormatter.clearConsole();
    }

    private void editSeatingClass() {
        ConsoleFormatter.clearConsole();
        ConsoleFormatter.printCenteredLine("Current seating class: " + this.currentSearch.getSeatingClass());
        ConsoleFormatter.printSeperatorLine();
        ConsoleFormatter.printBoxedLine("Available seating classes:");
        ConsoleFormatter.printBoxedLine("1. First Class");
        ConsoleFormatter.printBoxedLine("2. Second Class");
        ConsoleFormatter.printSeperatorLine();
        ConsoleFormatter.printPrompt("Enter your choice or press Enter to skip: ");

        try {
            String input = scanner.nextLine().trim();
            String seatingClass = "";

            switch (input) {
                case "1":
                    seatingClass = "First Class";
                    break;
                case "2":
                    seatingClass = "Second Class";
                    break;
                case "":
                    seatingClass = "";
                    break;
                default:
                    ConsoleFormatter.printBoxedLine("Invalid choice. No changes made.");
                    ConsoleFormatter.printPrompt("Press Enter to continue...");
                    scanner.nextLine();
                    return;
            }

            this.currentSearch.setSeatingClass(seatingClass);
        } catch (Exception e) {
            System.err.println(e);
        }

        ConsoleFormatter.clearConsole();
    }

    private void editPriceOptions() {
        ConsoleFormatter.clearConsole();
        ConsoleFormatter.printCenteredLine("Current price options: " + ConsoleFormatter.formatPriceInfo(currentSearch));
        ConsoleFormatter.printSeperatorLine();
        ConsoleFormatter.printBoxedLine("Modify which bound:");
        ConsoleFormatter.printBoxedLine("1. Minimum Price");
        ConsoleFormatter.printBoxedLine("2. Maximum Price");
        ConsoleFormatter.printSeperatorLine();
        ConsoleFormatter.printPrompt("Enter your choice or press Enter to skip: ");

        try {
            String input = scanner.nextLine().trim();

            switch (input) {
                case "1":
                    editMinPrice();
                    break;
                case "2":
                    editMaxPrice();
                    break;
                case "":
                default:
                    ConsoleFormatter.printBoxedLine("Invalid choice. No changes made.");
                    ConsoleFormatter.printPrompt("Press Enter to continue...");
                    scanner.nextLine();
                    return;
            }
        } catch (Exception e) {
            System.err.println(e);
        }

    }

    private void editMinPrice() {
        ConsoleFormatter.printSeperatorLine();
        ConsoleFormatter.printPrompt("Enter new minimum price (or Enter to skip): ");

        String input = scanner.nextLine();
        try {
            Double newMin = Double.parseDouble(input);

            Double currentMax = currentSearch.getMaxCost();
            if (currentMax != null && newMin > currentMax) {
                ConsoleFormatter.printBoxedLine("Invalid choice. No changes made.");
                ConsoleFormatter.printPrompt("Press Enter to continue...");
                scanner.nextLine();
                return;
            }
            currentSearch.setMinCost(newMin);
        } catch (Exception e) {
            // Empty
            currentSearch.setMinCost(null);
        }
        ConsoleFormatter.clearConsole();

    }

    private void editMaxPrice() {
        ConsoleFormatter.printSeperatorLine();
        ConsoleFormatter.printPrompt("Enter new maximum price (or Enter to skip): ");

        String input = scanner.nextLine();
        try {
            Double newMax = Double.parseDouble(input);

            Double currentMin = currentSearch.getMinCost();
            if (currentMin != null && newMax < currentMin) {
                ConsoleFormatter.printBoxedLine("Invalid choice. No changes made.");
                ConsoleFormatter.printPrompt("Press Enter to continue...");
                scanner.nextLine();
                return;
            }
            currentSearch.setMaxCost(newMax);

        } catch (Exception e) {
            // Empty
            currentSearch.setMaxCost(null);
        }
        ConsoleFormatter.clearConsole();
    }

    private void editSortingOptions() {
        ConsoleFormatter.clearConsole();
        ConsoleFormatter.printCenteredLine("Current sort by: " + this.currentSearch.getSortBy());
        ConsoleFormatter.printCenteredLine("Current order: " + this.currentSearch.getOrder());
        ConsoleFormatter.printSeperatorLine();
        ConsoleFormatter.printBoxedLine("Sort by options:");
        ConsoleFormatter.printBoxedLine("1. Departure Time");
        ConsoleFormatter.printBoxedLine("2. Arrival Time");
        ConsoleFormatter.printBoxedLine("3. Price");
        ConsoleFormatter.printBoxedLine("4. Duration");
        ConsoleFormatter.printSeperatorLine();
        ConsoleFormatter.printPrompt("Enter sort by choice (1-4) or press Enter to skip: ");

        try {
            String sortInput = scanner.nextLine().trim();
            String sortBy = "";

            switch (sortInput) {
                case "1":
                    sortBy = "Departure Time";
                    break;
                case "2":
                    sortBy = "Arrival Time";
                    break;
                case "3":
                    sortBy = "Price";
                    break;
                case "4":
                    sortBy = "Duration";
                    break;
                case "":
                    return; // Skip if empty
                default:
                    ConsoleFormatter.printBoxedLine("Invalid choice. No changes made.");
                    ConsoleFormatter.printPrompt("Press Enter to continue...");
                    scanner.nextLine();
                    return;
            }

            this.currentSearch.setSortBy(sortBy);

            // Ask for order
            ConsoleFormatter.printSeperatorLine();
            ConsoleFormatter.printBoxedLine("Sort order:");
            ConsoleFormatter.printBoxedLine("1. Ascending");
            ConsoleFormatter.printBoxedLine("2. Descending");
            ConsoleFormatter.printPrompt("Enter order choice (1-2): ");

            String orderInput = scanner.nextLine().trim();
            String order = "";

            switch (orderInput) {
                case "1":
                    order = "Ascending";
                    break;
                case "2":
                    order = "Descending";
                    break;
                default:
                    order = "Ascending"; // Default
                    break;
            }

            this.currentSearch.setOrder(order);

        } catch (Exception e) {
            System.err.println(e);
        }

        ConsoleFormatter.clearConsole();
    }

    private void resetSearch() {
        this.currentSearch = new Search();

        ConsoleFormatter.clearConsole();
        ConsoleFormatter.printCenteredLine("Current search has been reset.");
    }

    private void exitConsole() {
        ConsoleFormatter.clearConsole();
        ConsoleFormatter.printBorderLine();
        ConsoleFormatter.printCenteredLine("Thank you for using the European Rail Planning System!");
        ConsoleFormatter.printCenteredLine("We hope you have a wonderful journey!");
        ConsoleFormatter.printBorderLine();

        scanner.close();
        this.running = false;

        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        ConsoleFormatter.clearConsole();
    }

    private boolean isValidTimeFormat(String time) {
        if (time == null || time.trim().isEmpty()) {
            return true; // Allow empty times
        }

        try {
            String[] parts = time.split(":");
            if (parts.length != 2) {
                return false;
            }

            int hours = Integer.parseInt(parts[0]);
            int minutes = Integer.parseInt(parts[1]);

            return hours >= 0 && hours <= 23 && minutes >= 0 && minutes <= 59;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private boolean isValidDaysOfOperationFormat(String daysOfOperation) {
        if (daysOfOperation == null || daysOfOperation.trim().isEmpty()) {
            return true;
        }

        String input = daysOfOperation.trim();

        // Accept "Daily"
        if (input.equalsIgnoreCase("Daily")) {
            return true;
        }

        // Single range: Mon-Fri
        if (input.contains("-")) {
            if (input.contains(",")) {
                return false; // No mixed formats
            }

            String[] rangeParts = input.split("-");
            if (rangeParts.length != 2) {
                return false;
            }

            return isValidSingleDay(rangeParts[0].trim()) && isValidSingleDay(rangeParts[1].trim());
        }

        // Comma-separated: Mon,Wed,Fri
        if (input.contains(",")) {
            String[] parts = input.split(",");
            for (String part : parts) {
                if (!isValidSingleDay(part.trim())) {
                    return false;
                }
            }
            return true;
        }

        // Single day
        return isValidSingleDay(input);
    }

    private boolean isValidSingleDay(String day) {
        if (day == null || day.trim().isEmpty()) {
            return false;
        }

        String[] validDaysShort = { "Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun" };
        String[] validDaysFull = { "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday" };

        String trimmedDay = day.trim();

        // Check against short form
        for (String validDay : validDaysShort) {
            if (trimmedDay.equalsIgnoreCase(validDay)) {
                return true;
            }
        }

        // Check against full form
        for (String validDay : validDaysFull) {
            if (trimmedDay.equalsIgnoreCase(validDay)) {
                return true;
            }
        }

        return false;
    }

    private class ConsoleFormatter {
        private static final int CONSOLE_WIDTH = 80;
        private static final String BORDER_CHAR = "█";
        private static final String PADDING_CHAR = " ";

        private static final String[] COLUMN_TITLES = {
            "Departure City",
            "Arrival City",
            "Departure Time",
            "Arrival Time",
            "Train Type",
            "Days of Operation",
            "1st Class Rate",
            "2nd Class Rate",
            "Trip Duration",
            "Wait Time"
        };

        private static final int[] COLUMN_WIDTHS = {
            15, // Departure City
            13, // Arrival City
            15, // Departure Time
            13, // Arrival Time
            10, // Train Type
            18, // Days of Operation
            15, // 1st Class Rate
            15, // 2nd Class Rate
            14, // Trip Duration
            10  // Wait Time
        };

        private static final int SEPARTOR_WIDTH = Arrays.stream(COLUMN_WIDTHS).sum() + COLUMN_WIDTHS.length - 1;

        private ConsoleFormatter() {
        }

        private static void displayMenu(Search currentSearch) {
            printSeperatorLine();
            printCenteredLine("Search Filters");
            printSeperatorLine();
            printMenuItemWithValue("1. Departure City", currentSearch.getDepartureCity());
            printMenuItemWithValue("2. Arrival City", currentSearch.getArrivalCity());
            printMenuItemWithValue("3. Departure Time",
                    currentSearch.getDepartureDay() + " " + currentSearch.getDepartureTime());
            printMenuItemWithValue("4. Arrival Time",
                    currentSearch.getArrivalDay() + " " + currentSearch.getArrivalTime());
            printMenuItemWithValue("5. Train Type", currentSearch.getTrainType());
            printMenuItemWithValue("6. Days of Operation", currentSearch.getDaysOfOperation());
            printMenuItemWithValue("7. Seating Class Tier", currentSearch.getSeatingClass());
            printMenuItemWithValue("8. Min/Max Price", formatPriceInfo(currentSearch));
            printMenuItemWithValue("9. Sorting Options", formatSortingInfo(currentSearch));
            printSeperatorLine();
            printBoxedLine("0. Exit");
            printBoxedLine("R. Reset Search");
            printBoxedLine("S. Run Search");
            printSeperatorLine();
            printPrompt("Enter your selection: ");
        }

        private static String formatPriceInfo(Search currentSearch) {
            Double minPrice = currentSearch.getMinCost();
            Double maxPrice = currentSearch.getMaxCost();

            if (minPrice == null && maxPrice == null)
                return "Not set";
            else if (minPrice != null && maxPrice == null)
                return formatPrice(minPrice) + " ≤ Price";
            else if (minPrice == null && maxPrice != null)
                return "Price ≤ " + formatPrice(maxPrice);
            else
                return formatPrice(minPrice) + " ≤ Price ≤ " + formatPrice(maxPrice);
        }

        // Used to remove decimals when price is a whole number
        private static String formatPrice(Double price) {
            if (price == null)
                return "0";

            // Check if the price is a whole number
            if (price == Math.floor(price)) {
                return String.format("%.0f", price);
            } else {
                // Format to 2 decimal places and remove trailing zeros
                return String.format("%.2f", price).replaceAll("0*$", "").replaceAll("\\.$", "");
            }
        }

        private static String formatSortingInfo(Search currentSearch) {
            String sortBy = currentSearch.getSortBy();
            String order = currentSearch.getOrder();

            if ((sortBy == null || sortBy.trim().isEmpty()) &&
                    (order == null || order.trim().isEmpty())) {
                return "Not set";
            } else if (sortBy != null && !sortBy.trim().isEmpty() &&
                    order != null && !order.trim().isEmpty()) {
                return sortBy + " (" + order + ")";
            } else if (sortBy != null && !sortBy.trim().isEmpty()) {
                return sortBy;
            } else {
                return order;
            }
        }

        private static void printMenuItemWithValue(String menuItem, String currentValue) {
            int contentWidth = CONSOLE_WIDTH - 4; // Account for borders and padding

            // Handle empty or null values
            String displayValue = (currentValue == null || currentValue.trim().isEmpty()) ? "Not set" : currentValue;

            // Calculate available space for the menu item text
            int valueLength = displayValue.length();
            int availableSpace = contentWidth - valueLength - 1; // -1 for space between item and value

            // Truncate menu item if necessary
            String truncatedMenuItem = menuItem;
            if (menuItem.length() > availableSpace) {
                truncatedMenuItem = menuItem.substring(0, availableSpace - 3) + "...";
            }

            // Calculate padding between menu item and value
            int paddingLength = contentWidth - truncatedMenuItem.length() - valueLength;
            String padding = PADDING_CHAR.repeat(Math.max(1, paddingLength));

            System.out.println(BORDER_CHAR + " " + truncatedMenuItem + padding + displayValue + " " + BORDER_CHAR);
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

        private static void clearConsole() {
            try {
                // Windows implementation
                if (System.getProperty("os.name").contains("Windows")) {
                    new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
                } else {
                    // MacOs and Linux
                    System.out.print("\033[2J\033[H");
                    System.out.flush();
                }
            } catch (Exception e) {
                // Fallback
                for (int i = 0; i < 50; i++)
                    System.out.println();
            }
        }

        private static void printTableCell(String value, int width) {
            if (width < 3) {
                throw new IllegalArgumentException("Cell width is too small");
            }
            else if (value.length() > width) {
                System.out.print(value.substring(0, width - 3));
                System.out.print("...");
            }
            else {
                System.out.print(value);
                System.out.print(" ".repeat(width - value.length()));
            }
        }

        private static void printTripTableHeader() {
            System.out.println();

            for (int i = 0; i < COLUMN_TITLES.length; i++) {
                String title = COLUMN_TITLES[i];
                int width = COLUMN_WIDTHS[i];

                printTableCell(title, width);

                if (i != COLUMN_TITLES.length - 1) {
                    System.out.print("|");
                }
            }

            System.out.println();
            System.out.println("-".repeat(SEPARTOR_WIDTH));
        }

        private static void printTrip(Trip trip) {
            ArrayList<Connection> conns = trip.getConnections();

            // print the departure city
            printTableCell(trip.getDepartureCity().getCityName(), COLUMN_WIDTHS[0]);
            System.out.print("|");

            // print the arrival city
            printTableCell(trip.getArrivalCity().getCityName(), COLUMN_WIDTHS[1]);
            System.out.print("|");

            // print the departure time
            printTableCell(trip.getDepartureDate(), COLUMN_WIDTHS[2]);
            System.out.print("|");

            // print the arrival time
            printTableCell(trip.getArrivalDate(), COLUMN_WIDTHS[3]);
            System.out.print("|");

            // print the train type
            printTableCell(conns.get(0).trainType, COLUMN_WIDTHS[4]);
            System.out.print("|");

            // print the days of operation
            printTableCell(conns.get(0).getDaysOfOperation(), COLUMN_WIDTHS[5]);
            System.out.print("|");

            // print the first class ticket rate
            printTableCell(String.format("%.2f", trip.getFirstClassTicketRate()), COLUMN_WIDTHS[6]);
            System.out.print("|");

            // print the second class ticket rate
            printTableCell(String.format("%.2f", trip.getSecondClassTicketRate()), COLUMN_WIDTHS[7]);
            System.out.print("|");

            // print the trip duration
            printTableCell(trip.getTripDuration(), COLUMN_WIDTHS[8]);
            System.out.print("|");

            // print the wait time
            printTableCell(trip.getWaitTimeDuration(), COLUMN_WIDTHS[9]);

            // repeat train type and days of operation for each connection
            for (int i = 1; i < conns.size(); i++) {
                System.out.println();

                for (int j = 0; j < 4; j++) {
                    printTableCell("", COLUMN_WIDTHS[j]);
                    System.out.print("|");
                }

                // print the train type
                printTableCell(conns.get(i).trainType, COLUMN_WIDTHS[4]);
                System.out.print("|");

                // print the days of operation
                printTableCell(conns.get(i).getDaysOfOperation(), COLUMN_WIDTHS[5]);
                System.out.print("|");

                for (int j = 6; j < COLUMN_WIDTHS.length; j++) {
                    printTableCell("", COLUMN_WIDTHS[j]);
                    if (j != COLUMN_WIDTHS.length - 1) {
                        System.out.print("|");
                    }
                }
            }

            System.out.println();
            System.out.println("-".repeat(SEPARTOR_WIDTH));
        }
    }
}
