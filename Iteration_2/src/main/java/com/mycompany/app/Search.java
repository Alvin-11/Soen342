package com.mycompany.app;

public class Search {
    private String departureCity;
    private String arrivalCity;
    private String departureDay;
    private String departureTime;
    private String arrivalDay;
    private String arrivalTime;
    private String trainType;
    private String daysOfOperation;
    private Double minCost;
    private Double maxCost;

    private String seatingClass;
    private String sortBy;
    private String Order;

    public Search() {
        this.departureCity = "";
        this.arrivalCity = "";
        this.departureDay = "";
        this.departureTime = "";
        this.arrivalDay = "";
        this.arrivalTime = "";
        this.trainType = "";
        this.daysOfOperation = "";
        this.seatingClass = "";
        this.sortBy = "";
        this.Order = "";
    }

    public Double getMinCost() {
        return minCost;
    }

    public void setMinCost(Double minCost) {
        this.minCost = minCost;
    }

    public Double getMaxCost() {
        return maxCost;
    }

    public void setMaxCost(Double maxCost) {
        this.maxCost = maxCost;
    }

    public String getDepartureCity() {
        return departureCity;
    }

    public void setDepartureCity(String departureCity) {
        this.departureCity = departureCity;
    }

    public String getArrivalCity() {
        return arrivalCity;
    }

    public void setArrivalCity(String arrivalCity) {
        this.arrivalCity = arrivalCity;
    }

    public String getDepartureDay() {
        return departureDay;
    }

    public void setDepartureDay(String departureDay) {
        String[] validDaysShort = { "Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun" };
        String[] validDaysFull = { "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday" };

        String trimmedDay = departureDay.trim();

        for (int i = 0; i < 7; i++) {
            if (trimmedDay.equalsIgnoreCase(validDaysShort[i]) || trimmedDay.equalsIgnoreCase(validDaysFull[i])) {
                this.departureDay = validDaysFull[i];
                break;
            }
        }
    }

    public String getDepartureTime() {
        return departureTime;
    }

    public void setDepartureTime(String departureTime) {
        this.departureTime = departureTime;
    }

    public String getArrivalDay() {
        return arrivalDay;
    }

    public void setArrivalDay(String arrivalDay) {
        String[] validDaysShort = { "Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun" };
        String[] validDaysFull = { "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday" };

        String trimmedDay = arrivalDay.trim();

        for (int i = 0; i < 7; i++) {
            if (trimmedDay.equalsIgnoreCase(validDaysShort[i]) || trimmedDay.equalsIgnoreCase(validDaysFull[i])) {
                this.arrivalDay = validDaysFull[i];
                break;
            }
        }
    }

    public String getArrivalTime() {
        return arrivalTime;
    }

    public void setArrivalTime(String arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    public String getTrainType() {
        return trainType;
    }

    public void setTrainType(String trainType) {
        this.trainType = trainType;
    }

    public String getDaysOfOperation() {
        return daysOfOperation;
    }

    public void setDaysOfOperation(String daysOfOperation) {
        this.daysOfOperation = daysOfOperation;
    }

    public String getSeatingClass() {
        return seatingClass;
    }

    public void setSeatingClass(String seatingClass) {
        this.seatingClass = seatingClass;
    }

    public String getSortBy() {
        return sortBy;
    }

    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
    }

    public String getOrder() {
        return Order;
    }

    public void setOrder(String order) {
        Order = order;
    }

}
