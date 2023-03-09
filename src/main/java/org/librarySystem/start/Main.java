package org.librarySystem.start;

import java.security.NoSuchAlgorithmException;
import java.util.Scanner;

import static org.librarySystem.logic.Logic.*;

public class Main {
    public static void main(String[] args) throws NoSuchAlgorithmException {
        scanner = new Scanner(System.in);
        connectToDatabase();
        showMainMenu();
        closeConnection();
    }
}

