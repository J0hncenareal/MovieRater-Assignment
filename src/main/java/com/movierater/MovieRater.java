package com.movierater;

import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.*;
import java.util.Scanner;

public class MovieRater {
    private static final String DB_URL = "jdbc:sqlite:movierater.db";

   public static void main(String[] args) {
    initializeDatabase();
    importDataFromCSV("movie_habits.csv"); 
    runMenu();
}


    private static void initializeDatabase() {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {
            
            // Create tables
            stmt.execute("CREATE TABLE IF NOT EXISTS User (UserID INTEGER PRIMARY KEY, Age INTEGER NOT NULL, Email TEXT)");
            stmt.execute("CREATE TABLE IF NOT EXISTS Movie (MovieID INTEGER PRIMARY KEY, Title TEXT NOT NULL, ReleaseYear INTEGER, Director TEXT, Genre TEXT)");
            stmt.execute("CREATE TABLE IF NOT EXISTS ViewingHabit (UserID INTEGER, MovieID INTEGER, MinutesWatched INTEGER NOT NULL, PRIMARY KEY (UserID, MovieID), FOREIGN KEY (UserID) REFERENCES User(UserID), FOREIGN KEY (MovieID) REFERENCES Movie(MovieID))");
            
            System.out.println("Database initialized.");
        } catch (SQLException e) {
            System.err.println("Database initialization error: " + e.getMessage());
        }
    }

    private static void importDataFromCSV(String csvPath) {
        String line;
        try (Connection conn = DriverManager.getConnection(DB_URL);
             BufferedReader br = new BufferedReader(new FileReader(csvPath))) {
            
            conn.setAutoCommit(false);
            br.readLine(); // Skip header

            String userSql = "INSERT OR IGNORE INTO User (UserID, Age) VALUES (?, ?)";
            String movieSql = "INSERT OR IGNORE INTO Movie (MovieID, Title, ReleaseYear, Director, Genre) VALUES (?, ?, ?, ?, ?)";
            String habitSql = "INSERT OR IGNORE INTO ViewingHabit (UserID, MovieID, MinutesWatched) VALUES (?, ?, ?)";

            PreparedStatement userPstmt = conn.prepareStatement(userSql);
            PreparedStatement moviePstmt = conn.prepareStatement(movieSql);
            PreparedStatement habitPstmt = conn.prepareStatement(habitSql);

            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");
                // UserID,Age,MovieID,Title,ReleaseYear,Director,Genre,MinutesWatched
                int userId = Integer.parseInt(data[0]);
                int age = Integer.parseInt(data[1]);
                int movieId = Integer.parseInt(data[2]);
                String title = data[3];
                int year = Integer.parseInt(data[4]);
                String director = data[5];
                String genre = data[6];
                int minutes = Integer.parseInt(data[7]);

                userPstmt.setInt(1, userId);
                userPstmt.setInt(2, age);
                userPstmt.executeUpdate();

                moviePstmt.setInt(1, movieId);
                moviePstmt.setString(2, title);
                moviePstmt.setInt(3, year);
                moviePstmt.setString(4, director);
                moviePstmt.setString(5, genre);
                moviePstmt.executeUpdate();

                habitPstmt.setInt(1, userId);
                habitPstmt.setInt(2, movieId);
                habitPstmt.setInt(3, minutes);
                habitPstmt.executeUpdate();
            }
            conn.commit();
            System.out.println("Data imported from CSV.");
        } catch (Exception e) {
            System.err.println("Data import error: " + e.getMessage());
        }
    }

    private static void runMenu() {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("\n--- MovieRater Menu ---");
            System.out.println("1. Add a user");
            System.out.println("2. View user habits");
            System.out.println("3. Update movie title");
            System.out.println("4. Delete viewing record");
            System.out.println("5. Mean age of users");
            System.out.println("6. Total users for a movie");
            System.out.println("7. Total minutes watched");
            System.out.println("8. Users with >1 movie");
            System.out.println("9. Exit");
            System.out.print("Choose an option: ");

            int choice = scanner.nextInt();
            scanner.nextLine(); // consume newline

            switch (choice) {
                case 1: addUser(scanner); break;
                case 2: viewUserHabits(scanner); break;
                case 3: updateMovieTitle(scanner); break;
                case 4: deleteViewingRecord(scanner); break;
                case 5: showMeanAge(); break;
                case 6: showTotalUsersForMovie(scanner); break;
                case 7: showTotalMinutes(); break;
                case 8: showMultiMovieUsers(); break;
                case 9: return;
                default: System.out.println("Invalid choice.");
            }
        }
    }

    private static void addUser(Scanner scanner) {
        System.out.print("Enter User ID: ");
        int id = scanner.nextInt();
        System.out.print("Enter Age: ");
        int age = scanner.nextInt();
        scanner.nextLine();
        System.out.print("Enter Email: ");
        String email = scanner.nextLine();

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement("INSERT INTO User (UserID, Age, Email) VALUES (?, ?, ?)")) {
            pstmt.setInt(1, id);
            pstmt.setInt(2, age);
            pstmt.setString(3, email);
            pstmt.executeUpdate();
            System.out.println("User added successfully.");
        } catch (SQLException e) {
            System.err.println("Error adding user: " + e.getMessage());
        }
    }

    private static void viewUserHabits(Scanner scanner) {
        System.out.print("Enter User ID: ");
        int id = scanner.nextInt();

        String sql = "SELECT M.Title, VH.MinutesWatched FROM ViewingHabit VH JOIN Movie M ON VH.MovieID = M.MovieID WHERE VH.UserID = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            System.out.println("Viewing habits for User " + id + ":");
            while (rs.next()) {
                System.out.println("- " + rs.getString("Title") + ": " + rs.getInt("MinutesWatched") + " mins");
            }
        } catch (SQLException e) {
            System.err.println("Error viewing habits: " + e.getMessage());
        }
    }

    private static void updateMovieTitle(Scanner scanner) {
        System.out.print("Enter Movie ID: ");
        int id = scanner.nextInt();
        scanner.nextLine();
        System.out.print("Enter New Title: ");
        String title = scanner.nextLine();

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement("UPDATE Movie SET Title = ? WHERE MovieID = ?")) {
            pstmt.setString(1, title);
            pstmt.setInt(2, id);
            pstmt.executeUpdate();
            System.out.println("Movie title updated.");
        } catch (SQLException e) {
            System.err.println("Error updating title: " + e.getMessage());
        }
    }

    private static void deleteViewingRecord(Scanner scanner) {
        System.out.print("Enter User ID: ");
        int userId = scanner.nextInt();
        System.out.print("Enter Movie ID: ");
        int movieId = scanner.nextInt();

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement("DELETE FROM ViewingHabit WHERE UserID = ? AND MovieID = ?")) {
            pstmt.setInt(1, userId);
            pstmt.setInt(2, movieId);
            pstmt.executeUpdate();
            System.out.println("Record deleted.");
        } catch (SQLException e) {
            System.err.println("Error deleting record: " + e.getMessage());
        }
    }

    private static void showMeanAge() {
    // Open the connection and create a statement
    try (Connection conn = DriverManager.getConnection(DB_URL);
         Statement stmt = conn.createStatement();
         // Execute the SQL query in the SQL file
         ResultSet rs = stmt.executeQuery("SELECT AVG(Age) FROM User")) {
        
        if (rs.next()) {
            
            double average = rs.getDouble(1);
            System.out.println("The mean age of all users is: " + average);
        }
    } catch (SQLException e) {
       
        System.err.println("Database error: " + e.getMessage());
    }
}


        private static void showTotalUsersForMovie(Scanner scanner) {
        System.out.print("Enter Movie ID: ");
        int id = scanner.nextInt();
        
        // SQL query to count unique users who watched a specific movie
        String sql = "SELECT COUNT(DISTINCT UserID) FROM ViewingHabit WHERE MovieID = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            // Set the Movie ID parameter in the query
            pstmt.setInt(1, id);
            
            // Execute the query and get the result
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                int count = rs.getInt(1);
                System.out.println("Total unique users who watched Movie " + id + ": " + count);
            }
        } catch (SQLException e) {
            System.err.println("Error counting users for movie: " + e.getMessage());
        }
    }


    private static void showTotalMinutes() {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT SUM(MinutesWatched) FROM ViewingHabit")) {
            if (rs.next()) {
                System.out.println("Total minutes watched: " + rs.getInt(1));
            }
        } catch (SQLException e) {
            System.err.println("Error calculating total minutes: " + e.getMessage());
        }
    }

    private static void showMultiMovieUsers() {
        String sql = "SELECT COUNT(*) FROM (SELECT UserID FROM ViewingHabit GROUP BY UserID HAVING COUNT(MovieID) > 1)";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                System.out.println("Users who watched more than one movie: " + rs.getInt(1));
            }
        } catch (SQLException e) {
            System.err.println("Error counting multi-movie users: " + e.getMessage());
        }
    }
}
