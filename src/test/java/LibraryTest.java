import org.librarySystem.logic.Logic;
import org.junit.*;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static org.junit.Assert.*;


public class LibraryTest {
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;
    private final InputStream originalIn = System.in;
    private static Connection conn;

    @BeforeClass
    public static void setUpClass(){
        final String DB_URL = "jdbc:mysql://localhost/library";
        final String DB_USER = "root";
        final String DB_PASSWORD = "";
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
        } catch (ClassNotFoundException e) {
            System.err.println("Failed to load JDBC driver.");
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("Failed to connect to database.");
            e.printStackTrace();
        }
    }

    @AfterClass
    public static void tearDownClass() throws SQLException {
        conn.close();
    }

    @Before
    public void setUpStreams() {
        System.setOut(new PrintStream(outContent));
    }

    @After
    public void restoreStreams() {
        System.setOut(originalOut);
    }

    @Test
    public void testShowAllBooks() {
        Logic.connectToDatabase();
        Logic.showAllBooks();
        String expectedOutput = "View Books:\n" +
                "-----------------------------\n" +
                "Human by Author, ISBN: 11111, Is available: available\n" +
                "-----------------------------\n" +
                "Apocalipsis by 2023 new year, ISBN: 123456789, Is available: available\n" +
                "-----------------------------\n" +
                "asdfasc by ascasc, ISBN: 11231313, Is available: available\n" +
                "-----------------------------\n"+
                "sadfasc by ascasca, ISBN: 12345, Is available: available\n" +
                "-----------------------------\n"+
                "Loin by Gost, ISBN: 33333, Is available: rented\n" +
                "-----------------------------\n";
        assertEquals(expectedOutput, outContent.toString());
        Logic.closeConnection();
    }

    @Test
    public void testFindBooksByParameters() {
        Logic.connectToDatabase();
        String input = "33333";
        Logic.findBooksByParameters(input);
        String expectedOutput = "Search results:\n" +
                "Loin by Gost, ISBN: 33333, Status: rented, Rented by Adam on 2023-03-03, due on 2023-03-17\n";
        assertEquals(expectedOutput, outContent.toString());
        Logic.closeConnection();
    }
}