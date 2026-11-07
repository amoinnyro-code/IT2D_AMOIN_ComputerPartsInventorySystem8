package com.inventory.ui;

import com.inventory.dao.ActivityLogDao;
import com.inventory.dao.DatabaseConnection;
import java.sql.*;
import java.util.Scanner;

public class Authentication {
    private final ActivityLogDao logDao = new ActivityLogDao();

    public void start() {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("\n=== COMPUTER PARTS INVENTORY SYSTEM ===");
            System.out.println("[1] Login");
            System.out.println("[2] Register Account");
            System.out.println("[3] Exit");
            System.out.print("Select option: ");

            if (!scanner.hasNextInt()) {
                System.out.println("Invalid input! Please enter a number.");
                scanner.nextLine();
                continue;
            }

            int choice = scanner.nextInt();
            scanner.nextLine();

            if (choice == 1) {
                System.out.println("\n==========================================");
                System.out.println("   COMPUTER PARTS INVENTORY SYSTEM LOGIN   ");
                System.out.println("==========================================");
                System.out.print("Enter username: ");
                String username = scanner.nextLine().trim();

                System.out.print("Enter password: ");
                String password = scanner.nextLine().trim();

                if (username.equals("admin") && password.equals("admin123")) {
                    System.out.println("\nLogin Successful! Welcome, Admin.");
                    logDao.logAction(1, "LOGIN", "Administrator logged in successfully.");
                    AdminMenu adminMenu = new AdminMenu();
                    adminMenu.displayAuditLogMenu(scanner);
                } else {
                    authenticateUserFromDb(username, password, scanner);
                }
            } else if (choice == 2) {
                registerAccount(scanner);
            } else if (choice == 3) {
                System.out.println("Exiting application. Goodbye!");
                System.exit(0);
            } else {
                System.out.println("Invalid option. Try again.");
            }
        }
    }

    private void registerAccount(Scanner scanner) {
        System.out.println("\n==========================================");
        System.out.println("         REGISTER NEW ACCOUNT             ");
        System.out.println("==========================================");
        System.out.print("Enter username: ");
        String username = scanner.nextLine().trim();

        System.out.print("Enter password: ");
        String password = scanner.nextLine().trim();

        String sql = "INSERT INTO users (username, password, role, status) VALUES (?, ?, 'USER', 'PENDING')";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            pstmt.executeUpdate();

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int newUserId = generatedKeys.getInt(1);
                    System.out.println("\nRegistration Successful! User ID: " + newUserId + " (Status: PENDING approval by Admin)");
                    logDao.logAction(newUserId, "REGISTER", "New user account registered: " + username + " (PENDING)");
                }
            }
        } catch (SQLException e) {
            System.err.println("Registration failed (Username may already exist): " + e.getMessage());
        }
    }

    private void authenticateUserFromDb(String username, String password, Scanner scanner) {
        String sql = "SELECT id, username, status FROM users WHERE username = ? AND password = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String status = rs.getString("status");
                    if (!"APPROVED".equalsIgnoreCase(status)) {
                        System.out.println("\nLogin Failed: Your account status is " + status + ". Please wait for Admin approval.\n");
                        return;
                    }
                    int userId = rs.getInt("id");
                    System.out.println("\nLogin Successful! Welcome, " + username + ".");
                    logDao.logAction(userId, "LOGIN", "User [" + username + "] logged in successfully.");
                } else {
                    System.out.println("\nInvalid credentials! Please try again.\n");
                }
            }
        } catch (SQLException e) {
            System.err.println("Database error during login: " + e.getMessage());
        }
    }
}