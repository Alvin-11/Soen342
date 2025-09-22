package com.mycompany.app;

import java.io.FileReader;
import com.opencsv.CSVReader;

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

            //ignore the header
            csvReader.readNextSilently();

            //loop through every row in the CSV file
            while ((row = csvReader.readNext()) != null) {

                //unpack the CSV row
                String routeID               = row[0];
                String departureCityName     = row[1];
                String arrivalCityName       = row[2];
                String departureTime         = row[3];
                String arrivalTime           = row[4];
                String trainType             = row[5];
                String daysOfOperation       = row[6];
                double firstClassTicketRate  = Double.parseDouble(row[7]);
                double secondClassTicketRate = Double.parseDouble(row[8]);

                //check if the departure and arrival cities already exist
                City departureCity = cityCatalog.getCity(departureCityName);
                City arrivalCity   = cityCatalog.getCity(arrivalCityName);
                
                if (departureCity == null) {
                    departureCity = new City(departureCityName);
                    cityCatalog.addCity(departureCity);
                }

                if (arrivalCity == null) {
                    arrivalCity = new City(arrivalCityName);
                    cityCatalog.addCity(arrivalCity);
                }

                //create the Connection object and add it to the catalog
                Connection conn = new Connection(routeID, departureCity, arrivalCity, departureTime, arrivalTime, trainType, daysOfOperation, firstClassTicketRate, secondClassTicketRate);
                connectionCatalog.addConnection(conn);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}