package org.librarySystem.logic;

import org.librarySystem.user.User;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.util.InputMismatchException;
import java.util.Scanner;

public class Logic {
    private static final String DB_URL = "jdbc:mysql://localhost/library";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "";
    private static final String INSERT_USER = "INSERT INTO `users` (`first_name`, `last_name`, `password`) VALUES (?, ?, ?)";
    private static final String SELECT_USER = "SELECT * FROM `users` WHERE first_name = ? AND password = ?";
    private static final String SELECT_BOOKS_BY_PARAMETERS =
            "SELECT b.id, b.title, b.author, b.isbn, b.status, r.user_id, r.rented_date, r.due_date " +
                    "FROM books b LEFT JOIN rented r ON b.id = r.book_id " +
                    "WHERE b.title LIKE ? OR b.author LIKE ? OR b.isbn LIKE ?";
    private static final String SELECT_RENTAL_BY_BOOK_ID = "SELECT rented.*, users.first_name, users.last_name " +
            "FROM rented " +
            "JOIN users ON rented.user_id = users.id " +
            "WHERE rented.book_id = ?";
    private static final String INSERT_BOOK = "INSERT INTO `books`( `title`, `author`, `isbn`) VALUES (?, ?, ?)";
    private static final String SELECT_OVERDUE_BOOKS = "SELECT * FROM rented WHERE due_date < CURRENT_DATE()";
    private static final String SELECT_BOOKS = "SELECT * FROM `books`";
    private static final String SELECT_BOOKS_BY_ID = "SELECT * FROM `books` WHERE id= ?";
    private static final String SELECT_USER_BY_ID = "SELECT * FROM `users` WHERE id= ?";
    private static final String SELECT_RENTED_BOOKS = "SELECT b.title, u.first_name, u.last_name, r.rented_date, r.due_date " +
            "FROM Books b " +
            "JOIN Rented r ON b.id = r.book_id " +
            "JOIN Users u ON r.user_id = u.id " +
            "WHERE b.status = 'rented'";
    private static final String RENT_BOOK = "INSERT INTO rented (user_id, book_id, rented_date, due_date) VALUES (?, ?, CURDATE(), DATE_ADD(CURDATE(), INTERVAL 14 DAY))";
    private static final String CHECK_IS_BOOK_RENTED = "SELECT status, id FROM books WHERE isbn = ?";
    private static final String UPDATE_BOOK_AVAILABLE_STATUS = "UPDATE books SET status = ? WHERE id = ?";
    private static final String HASH_ALGORITHM = "SHA-256";

    private static Connection conn;
    public static Scanner scanner;
    static User user = new User();

    public static void connectToDatabase() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
        } catch (ClassNotFoundException e) {
            System.out.println("Failed to load JDBC driver.");
            e.printStackTrace();
        } catch (SQLException e) {
            System.out.println("Failed to connect to database.");
            e.printStackTrace();
        }
    }


    /**
     *  This method is used to log in a user to the library system.
     *  It prompts the user to enter their first name and password, and then hashes the password using the SHA-256 algorithm.
     *  The hashed password is then used to query the database for the corresponding user.
     *  If a user is found, the method logs the user in and displays the library menu.
     *  If no user is found, the method prints an error message.
     *  @throws NoSuchAlgorithmException if the specified hashing algorithm is not available on the system
     */
    private static void login() throws NoSuchAlgorithmException {
        System.out.println("Login:");
        System.out.print("Enter your first name: ");
        String firstName = scanner.nextLine();
        System.out.print("Enter your password: ");
        String password = scanner.nextLine();
        MessageDigest md = MessageDigest.getInstance(HASH_ALGORITHM);
        md.update(password.getBytes());
        byte[] byteData = md.digest();
        StringBuilder sb = new StringBuilder();
        for (byte b : byteData) {
            sb.append(Integer.toString((b & 0xff) + 0x100, 16).substring(1));
        }
        String hashedPassword = sb.toString();
        try {
            PreparedStatement stmt = conn.prepareStatement(SELECT_USER);
            stmt.setString(1, firstName);
            stmt.setString(2, hashedPassword);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                System.out.println("Login successful!");
                user = new User(rs.getInt("id"), firstName, password);
                showLibraryMenu();
            } else {
                System.err.println("Invalid username or password!");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**

     *  This method is used to register a new user to the library system.
     *  It prompts the user to enter their first name, last name, password and confirm password.
     *  It then validates that the passwords match and hashes the password using the SHA-256 algorithm.
     *  The hashed password and user information is then inserted into the database.
     *  If the registration is successful, the method prints a success message.
     *  If the registration fails, the method prints an error message.
     *  @throws NoSuchAlgorithmException if the specified hashing algorithm is not available on the system
     */
    private static void register() throws NoSuchAlgorithmException {
        System.out.print("Write your first name: ");
        String firstName = scanner.nextLine();
        System.out.print("Write your last name: ");
        String lastName = scanner.nextLine();
        String password = null;
        String confirmPassword = null;
        String hashedPassword = null;
        while (true) {
            System.out.print("Password: ");
            password = scanner.nextLine();
            System.out.print("Confirm password: ");
            confirmPassword = scanner.nextLine();
            if (password.equals(confirmPassword)) {
                MessageDigest md = MessageDigest.getInstance(HASH_ALGORITHM);
                md.update(password.getBytes());
                byte[] byteData = md.digest();
                StringBuilder sb = new StringBuilder();
                for (byte b : byteData) {
                    sb.append(Integer.toString((b & 0xff) + 0x100, 16).substring(1));
                }
                hashedPassword = sb.toString();
                break;
            }
            System.out.println("\nPasswords don't match. Please try again.\n");
        }
        try (PreparedStatement stmt = conn.prepareStatement(INSERT_USER)) {
            stmt.setString(1, firstName);
            stmt.setString(2, lastName);
            stmt.setString(3, hashedPassword);
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Registration successful!");
            } else {
                System.err.println("Registration failed!");
            }
        } catch (SQLException e) {
            System.err.println("Failed to register user.");
            e.printStackTrace();
        }
    }

    private static void showLibraryMenu() {
        System.out.println("Library Menu:");
        int choice = -1;
        do {
            System.out.println("1. Add book");
            System.out.println("2. Show all books");
            System.out.println("3. Find book by parameters: title, author, isbn");
            System.out.println("4. Rent book");
            System.out.println("5. Show rented books");
            System.out.println("6. Show books for which the return date is overdue");
            System.out.println("0. Logout");
            System.out.print("Enter your choice: ");
            try {
                choice = scanner.nextInt();
                scanner.nextLine();
                switch (choice) {
                    case 1:
                        addBook();
                        break;
                    case 2:
                        showAllBooks();
                        break;
                    case 3:
                        System.out.print("Enter name of the book or author or isbn: ");
                        String param = scanner.nextLine();
                        findBooksByParameters(param);
                        break;
                    case 4:
                        rentBook();
                        break;
                    case 5:
                        showRentedBooks();
                        break;
                    case 6:
                        showOverdueBooks();
                        break;
                    case 0:
                        return;
                    default:
                        System.err.println("Invalid choice!");
                }
            } catch (InputMismatchException e) {
                System.err.println("Invalid input! Please enter a number.");
                scanner.nextLine();
            }
        } while (true);
    }

    private static void showRentedBooks() {
        System.out.println("List of rented books:");
        try {
            PreparedStatement stmt = conn.prepareStatement(SELECT_RENTED_BOOKS);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String title = rs.getString("title");
                String firstName = rs.getString("first_name");
                String lastName = rs.getString("last_name");
                Date rentedDate = rs.getDate("rented_date");
                Date dueDate = rs.getDate("due_date");
                System.out.println("Title: " + title);
                System.out.println("Rented by: " + firstName + " " + lastName);
                System.out.println("Rented date: " + rentedDate);
                System.out.println("Due date: " + dueDate);
                System.out.println("----------------------");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void showMainMenu() throws NoSuchAlgorithmException {
        System.out.println("Welcome to the library!");

        boolean exit = false;
        while (!exit) {
            System.out.println("\nMAIN MENU");
            System.out.println("1. Login");
            System.out.println("2. Register");
            System.out.println("3. Exit");

            int choice = -1;
            try {
                System.out.print("Enter your choice: ");
                choice = Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.err.println("Invalid input! Please enter a number.");
                continue;
            }

            switch (choice) {
                case 1:
                    login();
                    break;
                case 2:
                    register();
                    break;
                case 3:
                    exit = true;
                    System.out.println("Exiting the library... Goodbye!");
                    break;
                default:
                    System.err.println("Invalid choice! Please try again.");
                    break;
            }
        }
    }

    private static void showOverdueBooks() {
        try {
            PreparedStatement bookStmt = conn.prepareStatement(SELECT_OVERDUE_BOOKS);
            ResultSet rs = bookStmt.executeQuery();
            if (!rs.isBeforeFirst()) {
               System.err.println("No overdue books found.");
               return;
            }
            System.out.println("List of overdue books:");
            while (rs.next()) {
                String rentDate = rs.getString("rented_date");
                String due_Date = rs.getString("due_date");
                String bookId = rs.getString("book_id");
                String userId = rs.getString("user_id");
                PreparedStatement bookRnt = conn.prepareStatement(SELECT_BOOKS_BY_ID);
                bookRnt.setString(1, bookId);
                ResultSet bookRs = bookRnt.executeQuery();
                if(bookRs.next()) {
                    String title = bookRs.getString("title");
                    String author = bookRs.getString("author");
                    String isbn = bookRs.getString("isbn");
                    System.out.println("Title: " + title + "Author: " + author + "ISBN: " + isbn);
                    System.out.println("Rent date: " + rentDate);
                    System.out.println("Due date: " + due_Date);
                } else {
                    System.err.println("Not found" );
                }
                PreparedStatement userRnt = conn.prepareStatement(SELECT_USER_BY_ID);
                userRnt.setString(1, userId);
                ResultSet userRs = userRnt.executeQuery();
                if(userRs.next()) {
                    String name = userRs.getString("first_name");
                    String lastName = userRs.getString("last_name");
                    System.out.println("User is rented this book: " + name + " " + lastName);
                } else {
                    System.err.println("Not found" );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void rentBook() {
        System.out.print("Enter the ISBN of the book you want to rent: ");
        String isbn = scanner.nextLine();
        try {
            PreparedStatement stmt = conn.prepareStatement(CHECK_IS_BOOK_RENTED);
            stmt.setString(1, isbn);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String status = rs.getString("status");
                String id = rs.getString("id");
                String userId = String.valueOf(user.getId());
                System.out.println("Status: " + status + ", Id: " + id);
                if (status.equals("available")) {
                    PreparedStatement rentStmt = conn.prepareStatement(RENT_BOOK);
                    rentStmt.setString(1, userId);
                    rentStmt.setString(2, id);
                    rentStmt.executeUpdate();
                    PreparedStatement updateStmt = conn.prepareStatement(UPDATE_BOOK_AVAILABLE_STATUS);
                    updateStmt.setString(1, "rented");
                    updateStmt.setString(2, id);
                    updateStmt.executeUpdate();

                    System.out.println("The book was rented successfully!");
                } else {
                    System.err.println("The book is already rented!");
                }
            } else {
                System.err.println("The book was not found.");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static void addBook() {
        System.out.println("Add Book:");
        System.out.print("Title: ");
        String title = scanner.nextLine();
        System.out.print("Author: ");
        String author = scanner.nextLine();
        System.out.print("ISBN: ");
        String isbn = scanner.nextLine();
        try (PreparedStatement stmt = conn.prepareStatement(INSERT_BOOK)) {
            stmt.setString(1, title);
            stmt.setString(2, author);
            stmt.setString(3, isbn);
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Book added successfully!");
            } else {
                System.err.println("Book add failed!");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void findBooksByParameters(String param) {
        try {
            PreparedStatement stmt = conn.prepareStatement(SELECT_BOOKS_BY_PARAMETERS);
            String wildcardQuery = "%" + param + "%";
            stmt.setString(1, wildcardQuery);
            stmt.setString(2, wildcardQuery);
            stmt.setString(3, wildcardQuery);
            ResultSet rs = stmt.executeQuery();

            if (!rs.isBeforeFirst()) {
                System.err.println("No books found.");
                return;
            }

            System.out.print("Search results:\n");
            while (rs.next()) {
                String bookId = rs.getString("id");
                String title = rs.getString("title");
                String author = rs.getString("author");
                String isbn = rs.getString("isbn");
                String status = rs.getString("status");

                // check if book is rented and get rental info
                if (status.equals("rented")) {
                    PreparedStatement rentalStmt = conn.prepareStatement(SELECT_RENTAL_BY_BOOK_ID);
                    rentalStmt.setString(1, bookId);
                    ResultSet rentalRs = rentalStmt.executeQuery();
                    if (rentalRs.next()) {
                        String userName = rentalRs.getString("first_name");
                        String rentalDate = rentalRs.getString("rented_date");
                        String dueDate = rentalRs.getString("due_date");;
                        System.out.print(title + " by " + author + ", ISBN: " + isbn + ", Status: " + status + ", Rented by " + userName + " on " + rentalDate + ", due on " + dueDate + "\n");
                    }
                } else {
                    System.out.print(title + " by " + author + ", ISBN: " + isbn + ", Status: " + status + "\n");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void showAllBooks() {
        System.out.print("View Books:\n");
        System.out.print("-----------------------------\n");
        try {
            PreparedStatement stmt = conn.prepareStatement(SELECT_BOOKS);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String title = rs.getString("title");
                String author = rs.getString("author");
                String isbn = rs.getString("isbn");
                String isAvailable = rs.getString("status");
                System.out.printf("%s by %s, ISBN: %s, Is available: %s\n", title, author, isbn, isAvailable);
                System.out.print("-----------------------------\n");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void closeConnection() {
        try {
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

