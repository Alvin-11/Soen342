package com.mycompany.app;

import java.util.Scanner;

public class CLI{
    private static final int CONSOLE_WIDTH = 80;
    private static final String BORDER_CHAR = "█";
    private static final String PADDING_CHAR = " ";
    private Scanner scanner;
    private boolean running;


    public CLI(){
        this.scanner = new Scanner(System.in);
        this.running = false;
    }

    void start(){
        this.running = true;
        printHeader("Welcome to the European Rail Planning System");

        while (running){
            displayMenu();
            handleUserInput();
        }
    }

    private void displayMenu(){
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
        printSeperatorLine();
        printPrompt("Enter your selection: ");
    }

    private void handleUserInput(){
        String s = scanner.next();
    }

    private void printPrompt(String prompt){
        System.out.print(BORDER_CHAR + " " + prompt);
    }

    private void printHeader(String title){
        printBorderLine();
        printCenteredLine(title);
        printBorderLine();
    }

    private void printBorderLine(){
        System.out.println(BORDER_CHAR.repeat(CONSOLE_WIDTH));
    }

    private void printCenteredLine(String text){
        int padding = (CONSOLE_WIDTH - text.length() - 2) / 2;
        String leftPadding = PADDING_CHAR.repeat(padding);
        String rightPadding = PADDING_CHAR.repeat(CONSOLE_WIDTH - text.length() - padding - 2);
        System.out.println(BORDER_CHAR + leftPadding + text + rightPadding + BORDER_CHAR);
    }

    private void printBoxedLine(String text){
        int contentWidth = CONSOLE_WIDTH - 4;
        if (text.length() > contentWidth){
            text = text.substring(0,contentWidth - 3)+ "...";
        }

        String padding = PADDING_CHAR.repeat(contentWidth - text.length());
        System.out.println(BORDER_CHAR + " " + text + padding + " " + BORDER_CHAR);
    }

    private void printSeperatorLine(){
        System.out.println(BORDER_CHAR+"-".repeat(CONSOLE_WIDTH-2) +BORDER_CHAR);
    }
}