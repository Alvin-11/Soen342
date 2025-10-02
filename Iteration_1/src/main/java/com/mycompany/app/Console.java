package com.mycompany.app;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
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
                    runSearch();
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

    public void runSearch() {
        ConsoleFormatter.clearConsole();
        ConsoleFormatter.printHeader("Search Results");

        // TODO: Implement actual search logic here

        ConsoleFormatter.printSeperatorLine();
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

    // This method essentials loops through the cities and their connections to find
    // all possible trips from 0 stop t0 2 stop that match the search criteria for
    // departure city and arrival city
    public ArrayList<Trip> filterbyDepartureCityAndArrivalCity(String DepartureCity, String ArrivalCity,
            String departureDay, String departureTime) {
        ArrayList<Trip> baseTrips = new ArrayList<Trip>();
        City departureCity = cityCatalog.getCity(DepartureCity);
        City arrivalCity = cityCatalog.getCity(ArrivalCity);

        if (departureCity == null | arrivalCity == null) {
            throw new IllegalArgumentException("One or both of the specified cities do not exist in the catalog.");
        }
        for (Connection conn : departureCity.outgoingConnections) {
            Connection[] connections = new Connection[1];
            connections[0] = conn;
            if (conn.arrivalCity.getCityName().equals(ArrivalCity)) { // Checking that the arrival city of the
                                                                      // connection is the same for a direct connection
                Trip trip;
                if (departureDay.length() > 0 || departureTime.length() > 0) {
                    trip = new Trip(connections, departureDay, departureTime);
                } else {
                    trip = new Trip(connections);
                }
                baseTrips.add(trip);
            }
            City departureCity1 = conn.arrivalCity;
            for (Connection conn2 : departureCity1.outgoingConnections) {
                Connection[] connection2 = new Connection[2];
                connection2[0] = conn;
                connection2[1] = conn2;
                if (conn2.arrivalCity.getCityName().equals(ArrivalCity)) { // Checking that the arrival city of the
                                                                           // connection is the same for a 1-stop
                                                                           // Indirect connection
                    Trip trip;
                    if (departureDay.length() > 0 || departureTime.length() > 0) {
                        trip = new Trip(connection2, departureDay, departureTime);
                    } else {
                        trip = new Trip(connection2);
                    }
                    baseTrips.add(trip);
                }

                City departureCity2 = conn2.arrivalCity;
                for (Connection conn3 : departureCity2.outgoingConnections) {
                    Connection[] connection3 = new Connection[3];
                    connection3[0] = conn;
                    connection3[1] = conn2;
                    connection3[2] = conn3;
                    if (conn3.arrivalCity.getCityName().equals(ArrivalCity)) { // Checking that the arrival city of the
                                                                               // connection is the same for a 2-stop
                                                                               // Indirect connection
                        Trip trip;
                        if (departureDay.length() > 0 || departureTime.length() > 0) {
                            trip = new Trip(connection3, departureDay, departureTime);
                        } else {
                            trip = new Trip(connection3);
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

    public ArrayList<Trip> filterbyFirstClassTicketRate(ArrayList<Trip> trip, int upper, int lower) { // This method
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

    public ArrayList<Trip> filterbySecondClassTicketRate(ArrayList<Trip> trip, int upper, int lower) { // This method
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
            if (trip1.getArrivalDate().contains(ArrivalDay) & trip1.getArrivalDate().contains(ArrivalTime)) {
                filteredTrips.add(trip1);
            }
        }
        return filteredTrips;
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

    private void editDepartureTime() {
        ConsoleFormatter.clearConsole();
        ConsoleFormatter.printCenteredLine("Current departure time: " + this.currentSearch.getDepartureTime());
        ConsoleFormatter.printSeperatorLine();
        ConsoleFormatter.printBoxedLine("Format: HH:MM (24-hour format, e.g., 14:30)");
        ConsoleFormatter.printPrompt("Enter new departure time: ");

        try {
            String newDepartureTime = scanner.nextLine().trim();
            if (isValidTimeFormat(newDepartureTime) || newDepartureTime.isEmpty()) {
                this.currentSearch.setDepartureTime(newDepartureTime);
            } else {
                ConsoleFormatter.printBoxedLine("Invalid time format. Please use HH:MM format.");
                ConsoleFormatter.printPrompt("Press Enter to continue...");
                scanner.nextLine();
            }
        } catch (Exception e) {
            System.err.println(e);
        }

        ConsoleFormatter.clearConsole();
    }

    private void editArrivalTime() {
        ConsoleFormatter.clearConsole();
        ConsoleFormatter.printCenteredLine("Current arrival time: " + this.currentSearch.getArrivalTime());
        ConsoleFormatter.printSeperatorLine();
        ConsoleFormatter.printBoxedLine("Format: HH:MM (24-hour format, e.g., 14:30)");
        ConsoleFormatter.printPrompt("Enter new arrival time: ");

        try {
            String newArrivalTime = scanner.nextLine().trim();
            if (isValidTimeFormat(newArrivalTime) || newArrivalTime.isEmpty()) {
                this.currentSearch.setArrivalTime(newArrivalTime);
            } else {
                ConsoleFormatter.printBoxedLine("Invalid time format. Please use HH:MM format.");
                ConsoleFormatter.printPrompt("Press Enter to continue...");
                scanner.nextLine();
            }
        } catch (Exception e) {
            System.err.println(e);
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
            return true; // Allow empty input
        }

        String input = daysOfOperation.trim();

        // Check for "Daily" (case-insensitive)
        if (input.equalsIgnoreCase("Daily")) {
            return true;
        }

        String[] validDaysShort = { "Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun" };
        String[] validDaysFull = { "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday" };

        // Check if it's a single range (contains exactly one hyphen)
        if (input.contains("-")) {
            // Must be exactly one range, no commas allowed
            if (input.contains(",")) {
                return false; // No combination of ranges and comma-separated days
            }

            String[] rangeParts = input.split("-");
            if (rangeParts.length != 2) {
                return false; // Invalid range format
            }

            String startDay = rangeParts[0].trim();
            String endDay = rangeParts[1].trim();

            // Validate start and end days
            return isValidDay(startDay, validDaysShort, validDaysFull) &&
                    isValidDay(endDay, validDaysShort, validDaysFull);
        }

        // Check if it's comma-separated days (no hyphens allowed)
        if (input.contains(",")) {
            String[] parts = input.split(",");

            // Validate each individual day
            for (String part : parts) {
                part = part.trim();
                if (!isValidDay(part, validDaysShort, validDaysFull)) {
                    return false;
                }
            }
            return true;
        }

        // Single day
        return isValidDay(input, validDaysShort, validDaysFull);
    }

    private boolean isValidDay(String day, String[] validDaysShort, String[] validDaysFull) {
        // Check against short form
        for (String validDay : validDaysShort) {
            if (day.equalsIgnoreCase(validDay)) {
                return true;
            }
        }

        // Check against full form
        for (String validDay : validDaysFull) {
            if (day.equalsIgnoreCase(validDay)) {
                return true;
            }
        }

        return false;
    } // Helper class to contain all methods related to printing to the ✨console✨

    private class ConsoleFormatter {
        private static final int CONSOLE_WIDTH = 80;
        private static final String BORDER_CHAR = "█";
        private static final String PADDING_CHAR = " ";

        private ConsoleFormatter() {
        }

        private static void displayMenu(Search currentSearch) {
            printSeperatorLine();
            printCenteredLine("Search Filters");
            printSeperatorLine();
            printMenuItemWithValue("1. Departure City", currentSearch.getDepartureCity());
            printMenuItemWithValue("2. Arrival City", currentSearch.getArrivalCity());
            printMenuItemWithValue("3. Departure Time", currentSearch.getDepartureTime());
            printMenuItemWithValue("4. Arrival Time", currentSearch.getArrivalTime());
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

    }
}
