package com.mycompany.app;

import java.io.FileReader;
import com.opencsv.CSVReader;
import java.util.*;
import java.util.stream.*;

public class Console {
    private CityCatalog cityCatalog;
    private ConnectionCatalog connectionCatalog;

    public Console() {
        this.cityCatalog = new CityCatalog();
        this.connectionCatalog = new ConnectionCatalog();
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
                String[] DaysInAWeek = { "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday" };
                String[] DaysInaWeekSmall = { "Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun" };
                ArrayList<String> daysofOperation1 = new ArrayList<String>();

                // Specify the days of the week the train operates
                if (daysOfOperation.equals("Daily")) {
                    for (String day : DaysInAWeek) {
                        daysofOperation1.add(day);
                    }
                } else {
                    String[] days = daysOfOperation.split(",");
                    for (String data : days) {
                        if (data.length() < 6) {
                            switch (data) {
                                case "Mon":
                                    daysofOperation1.add("Monday");
                                    break;
                                case "Tue":
                                    daysofOperation1.add("Tuesday");
                                    break;
                                case "Wed":
                                    daysofOperation1.add("Wednesday");
                                    break;
                                case "Thu":
                                    daysofOperation1.add("Thursday");
                                    break;
                                case "Fri":
                                    daysofOperation1.add("Friday");
                                    break;
                                case "Sat":
                                    daysofOperation1.add("Saturday");
                                    break;
                                case "Sun":
                                    daysofOperation1.add("Sunday");
                                    break;

                            }
                        } else if (data.length() == 7) {
                            String[] dayStrings = data.split("-");
                            int startIndex = 0;
                            int endIndex = 0;
                            switch (dayStrings[0]) {
                                case "Mon":
                                    startIndex = 0;
                                    break;
                                case "Tue":
                                    startIndex = 1;
                                    break;
                                case "Wed":
                                    startIndex = 2;
                                    break;
                                case "Thu":
                                    startIndex = 3;
                                    break;
                                case "Fri":
                                    startIndex = 4;
                                    break;
                                case "Sat":
                                    startIndex = 5;
                                    break;
                                case "Sun":
                                    startIndex = 6;
                                    break;

                            }
                            switch (dayStrings[1]) {
                                case "Mon":
                                    endIndex = 0;
                                    break;
                                case "Tue":
                                    endIndex = 1;
                                    break;
                                case "Wed":
                                    endIndex = 2;
                                    break;
                                case "Thu":
                                    endIndex = 3;
                                    break;
                                case "Fri":
                                    endIndex = 4;
                                    break;
                                case "Sat":
                                    endIndex = 5;
                                    break;
                                case "Sun":
                                    endIndex = 6;
                                    break;

                            }
                            for (int i = startIndex; i < endIndex + 1; i++) {
                                daysofOperation1.add(DaysInAWeek[i]);

                            }

                        }
                    }
                }

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
                System.out.println("Added : " + routeID + " from " + departureCityName + " to " + arrivalCityName
                        + " Departure: " + departureTime + " Arrival: " + arrivalTime + " Train Type: " + trainType
                        + " Days of Operation: " + daysofOperation1 + " First Class Ticket Rate: "
                        + firstClassTicketRate
                        + " Second Class Ticket Rate: " + secondClassTicketRate);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ArrayList<Connection> ReturnAllConnectionsForDepartureCity(ArrayList<Connection> catalog,
            String departureCityUser) {
        ArrayList<Connection> newcatalog = catalog.stream().filter(c -> c.departureCity.equals(departureCityUser))
                .collect(Collectors.toCollection(ArrayList::new));
        return newcatalog;
    }

    public ArrayList<Connection> ReturnAllConnectionsForArrivalCity(ArrayList<Connection> catalog,
            String arrivalCityUser) {
        ArrayList<Connection> newcatalog = catalog.stream().filter(c -> c.arrivalCity.equals(arrivalCityUser))
                .collect(Collectors.toCollection(ArrayList::new));
        return newcatalog;
    }

    public ArrayList<Connection> ReturnAllConnectionsForTrainType(ArrayList<Connection> catalog,
            String trainTypeUser) {
        ArrayList<Connection> newcatalog = catalog.stream().filter(c -> c.trainType.equals(trainTypeUser))
                .collect(Collectors.toCollection(ArrayList::new));
        return newcatalog;
    }
}
