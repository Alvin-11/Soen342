package com.mycompany.app;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.stream.Collectors;

import com.opencsv.CSVReader;

public class Console {
    private CityCatalog cityCatalog;
    private  ConnectionCatalog connectionCatalog;
    public ArrayList<Connection> indirectConnectionsCatalog;

    public Console() {
        this.cityCatalog = new CityCatalog();
        this.connectionCatalog = new ConnectionCatalog();
        this.indirectConnectionsCatalog = new ArrayList<Connection>();
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

                // For the Indirect connections as a Deep copy of the original catalog
                Connection conn1 = new Connection(routeID, departureCity, arrivalCity, departureTime, arrivalTime,
                        trainType, daysofOperation1, firstClassTicketRate, secondClassTicketRate);
                indirectConnectionsCatalog.add(conn1);
                // System.out.println("Connection added: " + routeID);
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

    public CityCatalog SetCityConnections() {
        for(Connection conn : connectionCatalog.getAllConnections()){
            cityCatalog.getCitybyName(conn.departureCity.getCityName()).addArrivalCity(conn.arrivalCity.getCityName(), conn); 
            cityCatalog.getCitybyName(conn.arrivalCity.getCityName()).addDepartureCity(conn.departureCity.getCityName(), conn);
        }
        return cityCatalog;
    }
    
     public CityCatalog ReturnAllConnectionsForDepartureCityHashMap(CityCatalog catalog,
            String departureCityUser, int number) {
                CityCatalog newcatalog = new CityCatalog();
            }

    // Deep copy of an ArrayList of Connections
    public ArrayList<Connection> DeepCopy(ArrayList<Connection> catalog) {
        ArrayList<Connection> newcatalog = new ArrayList<Connection>();
        for (Connection conn : catalog) {
            City departureCity = new City(conn.departureCity.getCityName());
            City arrivalCity = new City(conn.arrivalCity.getCityName());
            Connection conn1 = new Connection(conn.getRouteID(), departureCity, arrivalCity,
                    conn.departureTime, conn.arrivalTime, conn.trainType, conn.daysOfOperation,
                    conn.firstClassTicketRate, conn.secondClassTicketRate);
            newcatalog.add(conn1);
        }
        return newcatalog;
    }

    // Prepare the indirect connections based on Arrival City and Departure City.
    // Produces the shallow copy of the original catalog.
    public ArrayList<Connection> PrepareIndirectConnections(ArrayList<Connection> catalog) {
        ArrayList<Connection> newcatalog = new ArrayList<Connection>();
        for (Connection conn1 : catalog) {
            Connection conn101 = new Connection(conn1.getRouteID(), conn1.departureCity, conn1.arrivalCity,
                    conn1.departureTime, conn1.arrivalTime, conn1.trainType, conn1.daysOfOperation,
                    conn1.firstClassTicketRate, conn1.secondClassTicketRate);
            for (Connection conn2 : catalog) {
                Connection conn201 = new Connection(conn2.getRouteID(), conn2.departureCity, conn2.arrivalCity,
                        conn2.departureTime, conn2.arrivalTime, conn2.trainType, conn2.daysOfOperation,
                        conn2.firstClassTicketRate, conn2.secondClassTicketRate);
                for (Connection conn3 : catalog) {
                    Connection conn301 = new Connection(conn3.getRouteID(), conn3.departureCity, conn3.arrivalCity,
                            conn3.departureTime, conn3.arrivalTime, conn3.trainType, conn3.daysOfOperation,
                            conn3.firstClassTicketRate, conn3.secondClassTicketRate);
                    if (conn2.arrivalCity.getCityName().equals(conn3.departureCity.getCityName())
                            && !conn2.departureCity.getCityName().equals(conn3.arrivalCity.getCityName())) {
                        conn201.connections.add(conn301);
                    }
                }
                if (conn1.arrivalCity.getCityName().equals(conn2.departureCity.getCityName())
                        && !conn1.departureCity.getCityName().equals(conn2.arrivalCity.getCityName())) {
                    conn101.connections.add(conn201);
                }
            }
            newcatalog.add(conn101);
        }
        return newcatalog;
    }

    // Below are the methods to filter through the parameters for One Direct
    // Connection . Produces shallow copy of the original catalog


    public ArrayList<Connection> ReturnAllConnectionsForDepartureCity(ArrayList<Connection> catalog,
            String departureCityUser) {
        return catalog.stream().filter(c -> c.departureCity.getCityName().equalsIgnoreCase(departureCityUser))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public ArrayList<Connection> ReturnAllConnectionsForArrivalCity(ArrayList<Connection> catalog,
            String arrivalCityUser, int number) {

        ArrayList<Connection> newcatalog = new ArrayList<Connection>();

        if (number == 0) {
            newcatalog = catalog.stream()
                    .filter(c -> c.arrivalCity.getCityName().equalsIgnoreCase(arrivalCityUser))
                    .collect(Collectors.toCollection(ArrayList::new));
        } else if (number == 1) {

            for (Connection conn1 : catalog) {
                Connection conn101 = new Connection(conn1.getRouteID(), conn1.departureCity, conn1.arrivalCity,
                        conn1.departureTime, conn1.arrivalTime, conn1.trainType, conn1.daysOfOperation,
                        conn1.firstClassTicketRate, conn1.secondClassTicketRate);
                for (Connection conn2 : conn1.connections) {
                    Connection conn201 = new Connection(conn2.getRouteID(), conn2.departureCity, conn2.arrivalCity,
                            conn2.departureTime, conn2.arrivalTime, conn2.trainType, conn2.daysOfOperation,
                            conn2.firstClassTicketRate, conn2.secondClassTicketRate);
                    if (conn2.arrivalCity.getCityName().equals(arrivalCityUser)) {
                        conn101.connections.add(conn201);
                    }
                }
                newcatalog.add(conn101);
            }
        } else if (number == 2) {
            for (Connection conn1 : catalog) {
                Connection conn101 = new Connection(conn1.getRouteID(), conn1.departureCity, conn1.arrivalCity,
                        conn1.departureTime, conn1.arrivalTime, conn1.trainType, conn1.daysOfOperation,
                        conn1.firstClassTicketRate, conn1.secondClassTicketRate);
                for (Connection conn2 : conn1.connections) {
                    Connection conn201 = new Connection(conn2.getRouteID(), conn2.departureCity, conn2.arrivalCity,
                            conn2.departureTime, conn2.arrivalTime, conn2.trainType, conn2.daysOfOperation,
                            conn2.firstClassTicketRate, conn2.secondClassTicketRate);
                    for (Connection conn3 : conn2.connections) {
                        if (conn3.arrivalCity.getCityName().equals(arrivalCityUser)) {
                            conn201.connections.add(conn3);
                        }
                    }
                    conn101.connections.add(conn201);
                }
                newcatalog.add(conn101);
            }
        }

        return newcatalog;
    }

    public ArrayList<Connection> ReturnAllConnectionsForTrainType(ArrayList<Connection> catalog,
            String trainTypeUser, int number) {
        ArrayList<Connection> newcatalog = new ArrayList<Connection>();
        if (number == 0) {
            newcatalog = catalog.stream().filter(c -> c.trainType.equalsIgnoreCase(trainTypeUser))
                    .collect(Collectors.toCollection(ArrayList::new));
        } else if (number == 1) {
            for (Connection conn1 : catalog) {
                Connection conn101 = new Connection(conn1.getRouteID(), conn1.departureCity, conn1.arrivalCity,
                        conn1.departureTime, conn1.arrivalTime, conn1.trainType, conn1.daysOfOperation,
                        conn1.firstClassTicketRate, conn1.secondClassTicketRate);
                if (conn1.trainType.equals(trainTypeUser)) {
                    for (Connection conn2 : conn1.connections) {
                        Connection conn201 = new Connection(conn2.getRouteID(), conn2.departureCity, conn2.arrivalCity,
                                conn2.departureTime, conn2.arrivalTime, conn2.trainType, conn2.daysOfOperation,
                                conn2.firstClassTicketRate, conn2.secondClassTicketRate);
                        if (conn2.trainType.equals(trainTypeUser)) {
                            conn101.connections.add(conn201);
                        }
                    }
                }
                newcatalog.add(conn101);
            }
        } else if (number == 2) {
            for (Connection conn1 : catalog) {
                Connection conn101 = new Connection(conn1.getRouteID(), conn1.departureCity, conn1.arrivalCity,
                        conn1.departureTime, conn1.arrivalTime, conn1.trainType, conn1.daysOfOperation,
                        conn1.firstClassTicketRate, conn1.secondClassTicketRate);
                if (conn1.trainType.equals(trainTypeUser)) {
                    for (Connection conn2 : conn1.connections) {
                        Connection conn201 = new Connection(conn2.getRouteID(), conn2.departureCity, conn2.arrivalCity,
                                conn2.departureTime, conn2.arrivalTime, conn2.trainType, conn2.daysOfOperation,
                                conn2.firstClassTicketRate, conn2.secondClassTicketRate);
                        if (conn2.trainType.equals(trainTypeUser)) {
                            for (Connection conn3 : conn2.connections) {
                                if (conn3.trainType.equals(trainTypeUser)) {
                                    conn201.connections.add(conn3);
                                }
                            }
                        }
                        conn101.connections.add(conn201);
                    }
                }
                newcatalog.add(conn101);
            }
        }
        return newcatalog;
    }

    public ArrayList<Connection> ReturnAllConnectionsForFirstClassTicket(ArrayList<Connection> catalog,
            double lowerPrice, double upperPrice, int number) {
        ArrayList<Connection> newcatalog = new ArrayList<Connection>();
        if (number == 0) {
            newcatalog = catalog.stream()
                    .filter(c -> c.firstClassTicketRate >= lowerPrice | c.firstClassTicketRate <= upperPrice)
                    .collect(Collectors.toCollection(ArrayList::new));
        } else if (number == 1) {
            for (Connection conn1 : catalog) {
                Connection conn101 = new Connection(conn1.getRouteID(), conn1.departureCity, conn1.arrivalCity,
                        conn1.departureTime, conn1.arrivalTime, conn1.trainType, conn1.daysOfOperation,
                        conn1.firstClassTicketRate, conn1.secondClassTicketRate);
                for (Connection conn2 : conn1.connections) {
                    Connection conn201 = new Connection(conn2.getRouteID(), conn2.departureCity, conn2.arrivalCity,
                            conn2.departureTime, conn2.arrivalTime, conn2.trainType, conn2.daysOfOperation,
                            conn2.firstClassTicketRate, conn2.secondClassTicketRate);
                    if ((conn2.firstClassTicketRate + conn1.firstClassTicketRate) >= lowerPrice
                            & (conn2.firstClassTicketRate + conn1.firstClassTicketRate) <= upperPrice) {
                        conn101.connections.add(conn201);
                    }
                }

                newcatalog.add(conn101);
            }
        } else if (number == 2) {
            for (Connection conn1 : catalog) {
                Connection conn101 = new Connection(conn1.getRouteID(), conn1.departureCity, conn1.arrivalCity,
                        conn1.departureTime, conn1.arrivalTime, conn1.trainType, conn1.daysOfOperation,
                        conn1.firstClassTicketRate, conn1.secondClassTicketRate);
                for (Connection conn2 : conn1.connections) {
                    Connection conn201 = new Connection(conn2.getRouteID(), conn2.departureCity, conn2.arrivalCity,
                            conn2.departureTime, conn2.arrivalTime, conn2.trainType, conn2.daysOfOperation,
                            conn2.firstClassTicketRate, conn2.secondClassTicketRate);
                    for (Connection conn3 : conn2.connections) {
                        if ((conn2.firstClassTicketRate + conn1.firstClassTicketRate
                                + conn3.firstClassTicketRate) >= lowerPrice
                                & (conn2.firstClassTicketRate + conn1.firstClassTicketRate
                                        + conn3.firstClassTicketRate) <= upperPrice) {
                            conn201.connections.add(conn3);
                        }
                    }
                    conn101.connections.add(conn201);
                }
                newcatalog.add(conn101);
            }
        }
        return newcatalog;
    }

    public ArrayList<Connection> ReturnAllConnectionsForSecondClassTicket(ArrayList<Connection> catalog,
            double lowerPrice, double upperPrice, int number) {
        ArrayList<Connection> newcatalog = new ArrayList<Connection>();
        if (number == 0) {
            newcatalog = catalog.stream()
                    .filter(c -> c.secondClassTicketRate >= lowerPrice | c.secondClassTicketRate <= upperPrice)
                    .collect(Collectors.toCollection(ArrayList::new));
        } else if (number == 1) {
            for (Connection conn1 : catalog) {
                Connection conn101 = new Connection(conn1.getRouteID(), conn1.departureCity, conn1.arrivalCity,
                        conn1.departureTime, conn1.arrivalTime, conn1.trainType, conn1.daysOfOperation,
                        conn1.firstClassTicketRate, conn1.secondClassTicketRate);
                for (Connection conn2 : conn1.connections) {
                    Connection conn201 = new Connection(conn2.getRouteID(), conn2.departureCity, conn2.arrivalCity,
                            conn2.departureTime, conn2.arrivalTime, conn2.trainType, conn2.daysOfOperation,
                            conn2.firstClassTicketRate, conn2.secondClassTicketRate);
                    if ((conn2.secondClassTicketRate + conn1.secondClassTicketRate) >= lowerPrice
                            & (conn2.secondClassTicketRate + conn1.secondClassTicketRate) <= upperPrice) {
                        conn101.connections.add(conn201);
                    }
                }
                newcatalog.add(conn101);
            }
        } else if (number == 2) {
            for (Connection conn1 : catalog) {
                Connection conn101 = new Connection(conn1.getRouteID(), conn1.departureCity, conn1.arrivalCity,
                        conn1.departureTime, conn1.arrivalTime, conn1.trainType, conn1.daysOfOperation,
                        conn1.firstClassTicketRate, conn1.secondClassTicketRate);
                for (Connection conn2 : conn1.connections) {
                    Connection conn201 = new Connection(conn2.getRouteID(), conn2.departureCity, conn2.arrivalCity,
                            conn2.departureTime, conn2.arrivalTime, conn2.trainType, conn2.daysOfOperation,
                            conn2.firstClassTicketRate, conn2.secondClassTicketRate);
                    for (Connection conn3 : conn2.connections) {
                        if ((conn2.secondClassTicketRate + conn1.secondClassTicketRate
                                + conn3.secondClassTicketRate) >= lowerPrice
                                & (conn2.secondClassTicketRate + conn1.secondClassTicketRate
                                        + conn3.secondClassTicketRate) <= upperPrice) {
                            conn201.connections.add(conn3);
                        }
                    }
                    conn101.connections.add(conn201);
                }
                newcatalog.add(conn101);
            }
        }
        return newcatalog;
    }
}
