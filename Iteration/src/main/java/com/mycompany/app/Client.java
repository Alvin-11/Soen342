package com.mycompany.app;

public class Client {
    private static int idCounter = 0;

    private final String clientID;
    private String firstName;
    private String lastName;
    private int age;
    private String passportStateID; // User's passport/state ID

    public Client(String firstName, String lastName, int age) {
        this.clientID = "C" + (++idCounter);
        this.firstName = firstName;
        this.lastName = lastName;
        this.age = age;
        this.passportStateID = null; // Will be set when needed
    }

    public Client(String firstName, String lastName, int age, String passportStateID) {
        this.clientID = "C" + (++idCounter);
        this.firstName = firstName;
        this.lastName = lastName;
        this.age = age;
        this.passportStateID = passportStateID;
    }

    // When reading from DB
    public Client(String clientID, String firstName, String lastName, int age, String passportStateID) {
        this.clientID = clientID;
        this.firstName = firstName;
        this.lastName = lastName;
        this.age = age;
        this.passportStateID = passportStateID;
    }

    public static void setIdCounter(int value) {
        if (value >= idCounter) {
            idCounter = value;
        }
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
    public String getPassportStateID() {
        return passportStateID;
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
    public void setPassportStateID(String passportStateID) {
        this.passportStateID = passportStateID;
    }
}
