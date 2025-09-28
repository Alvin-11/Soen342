package com.mycompany.app;

import java.io.FileReader;
import java.util.*;
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
                    editDepartureCity();
                    break;
                case 2:
                    editArrivalCity();
                    break;
                case 3:
                    editDepartureTime();
                    break;
                case 4:
                    editArrivalTime();
                    break;
                case 5:
                    editTrainType();
                    break;
                case 6:
                    editDaysOfOperation();
                    break;
                case 7:
                    editSeatingClass();
                    break;
                case 8:
                    editSortingOptions();
                    break;
                case 9:
                    resetSearch();
                case 0:
                    exitConsole();
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
                String daysOfOperation = row[6];
                double firstClassTicketRate = Double.parseDouble(row[7]);
                double secondClassTicketRate = Double.parseDouble(row[8]);
                ArrayList<String> daysofOperation1 = DaysOfOperationStringProcessing(daysOfOperation);

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
                        trainType, daysofOperation1, firstClassTicketRate, secondClassTicketRate);
                connectionCatalog.addConnection(conn);
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

    private void editDepartureCity() {
    }

    private void editArrivalCity() {
    }

    private void editDepartureTime() {
    }

    private void editArrivalTime() {
    }

    private void editTrainType() {
    }

    private void editDaysOfOperation() {
    }

    private void editSeatingClass() {
    }

    private void editSortingOptions(){}

    private void resetSearch(){}
    
    private void exitConsole(){}
    

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
            printBoxedLine("8. Sorting Options");
            printBoxedLine("9. Reset Search");
            printBoxedLine("0. Exit");

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
