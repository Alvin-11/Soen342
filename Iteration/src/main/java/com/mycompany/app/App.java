package com.mycompany.app;

public class App {
    public static void main(String[] args) {
        boolean debug = java.util.Arrays.asList(args).contains("--debug");
        System.setProperty("debug", Boolean.toString(debug));

        Console console = new Console();
        console.start();
    }
}