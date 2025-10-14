package com.mycompany.app;

public class Client {
    private String clientID;
    private String firstName;
    private String lastName;
    private int age;

    public Client(String clientID, String firstName, String lastName, int age) {
        this.clientID = clientID;
        this.firstName = firstName;
        this.lastName = lastName;
        this.age = age;
    }
    public String getClientID() {
        return clientID;
    }
    public String getFirstName() {
        return firstName;
    }
    public String getLastName() {
        return lastName;
    }
    public int getAge() {
        return age;
    }
    public void setClientID(String clientID) {
        this.clientID = clientID;
    }
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    public void setAge(int age) {
        this.age = age;
    }
}
