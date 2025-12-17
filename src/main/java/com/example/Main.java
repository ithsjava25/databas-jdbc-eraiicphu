package com.example;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Scanner;

public class Main {

    static void main(String[] args) {
        if (isDevMode(args)) {
            DevDatabaseInitializer.start();
        }
        new Main().run();
    }

    public void run() {
        // Resolve DB settings with precedence: System properties -> Environment variables
        String jdbcUrl = resolveConfig("APP_JDBC_URL", "APP_JDBC_URL");
        String dbUser = resolveConfig("APP_DB_USER", "APP_DB_USER");
        String dbPass = resolveConfig("APP_DB_PASS", "APP_DB_PASS");

        if (jdbcUrl == null || dbUser == null || dbPass == null) {
            throw new IllegalStateException(
                    "Missing DB configuration. Provide APP_JDBC_URL, APP_DB_USER, APP_DB_PASS " +
                            "as system properties (-Dkey=value) or environment variables.");
        }

        try (Connection connection = DriverManager.getConnection(jdbcUrl, dbUser, dbPass);
             Scanner scanner = new Scanner(System.in)) {

            System.out.println("Username:");
            String username = scanner.nextLine().trim();

            System.out.println("Password:");
            String password = scanner.nextLine().trim();

            Integer loggedInUserId = null;

            try (var ps = connection.prepareStatement(
                    "SELECT user_id FROM account WHERE name = ? AND password = ?")) {
                ps.setString(1, username);
                ps.setString(2, password);
                try (var rs = ps.executeQuery()) {
                    if (rs.next()) {
                        loggedInUserId = rs.getInt("user_id");
                    }
                }
            }

            if (loggedInUserId == null) {
                System.out.println("Invalid username or password");
                System.out.println("0) Exit");
                return;
            }


            while (true) {
                System.out.println("1) List moon missions");
                System.out.println("2) Get a moon mission by mission_id");
                System.out.println("3) Count missions for a given year");
                System.out.println("4) Create an account");
                System.out.println("5) Update an account password");
                System.out.println("6) Delete an account");
                System.out.println("0) Exit");

                String choice = scanner.nextLine().trim();

                switch (choice) {
                    case "1" -> listMissions(connection);
                    case "2" -> getMissionById(connection, scanner);
                    case "3" -> countMissionsByYear(connection, scanner);
                    case "4" -> createAccount(connection, scanner);
                    case "5" -> updateAccountPassword(connection, scanner);
                    case "6" -> deleteAccount(connection, scanner);
                    case "0" -> { return; }
                    default -> System.out.println("Invalid option");
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void listMissions(Connection connection) throws SQLException {
        try (var ps = connection.prepareStatement("SELECT spacecraft FROM moon_mission");
             var rs = ps.executeQuery()) {
            while (rs.next()) {
                System.out.println(rs.getString("spacecraft"));
            }
        }
    }

    private void getMissionById(Connection connection, Scanner scanner) throws SQLException {

    }

    private void countMissionsByYear(Connection connection, Scanner scanner) throws SQLException {

    }

    private void createAccount(Connection connection, Scanner scanner) throws SQLException {

    }

    private void updateAccountPassword(Connection connection, Scanner scanner) throws SQLException {

    }

    private void deleteAccount(Connection connection, Scanner scanner) throws SQLException {

    }


    /**
     * Determines if the application is running in development mode based on system properties,
     * environment variables, or command-line arguments.
     *
     * @param args an array of command-line arguments
     * @return {@code true} if the application is in development mode; {@code false} otherwise
     */
    private static boolean isDevMode(String[] args) {
        if (Boolean.getBoolean("devMode")) return true;
        if ("true".equalsIgnoreCase(System.getenv("DEV_MODE"))) return true;
        return Arrays.asList(args).contains("--dev");
    }

    /**
     * Reads configuration with precedence: Java system property first, then environment variable.
     * Returns trimmed value or null if neither source provides a non-empty value.
     */
    private static String resolveConfig(String propertyKey, String envKey) {
        String v = System.getProperty(propertyKey);
        if (v == null || v.trim().isEmpty()) {
            v = System.getenv(envKey);
        }
        return (v == null || v.trim().isEmpty()) ? null : v.trim();
    }
}
