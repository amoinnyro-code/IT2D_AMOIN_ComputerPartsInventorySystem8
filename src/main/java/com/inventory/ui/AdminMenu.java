package com.inventory.ui;

import com.inventory.dao.ActivityLogDao;
import com.inventory.dao.DatabaseConnection;
import com.inventory.model.ActivityLog;
import java.sql.*;
import java.util.List;
import java.util.Scanner;

public class AdminMenu {

    private final ActivityLogDao logDao = new ActivityLogDao();

    public void displayAuditLogMenu(Scanner scanner) {
        while (true) {
            System.out.println("\n==========================================");
            System.out.println("            ADMIN CONTROL PANEL           ");
            System.out.println("==========================================");
            System.out.println("[1] VIEW ALL ACTIVITY LOGS");
            System.out.println("[2] FILTER LOGS BY USER ID");
            System.out.println("[3] APPROVE PENDING USER ACCOUNTS");
            System.out.println("[4] LOGOUT / BACK TO LOGIN");
            System.out.println("==========================================");
            System.out.print("Enter your choice: ");

            if (!scanner.hasNextInt()) {
                System.out.println("Invalid input! Please enter a number.");
                scanner.nextLine();
                continue;
            }

            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1 -> renderLogTable(logDao.getAllLogs());
                case 2 -> {
                    System.out.print("Enter User ID to filter: ");
                    if (scanner.hasNextInt()) {
                        int targetId = scanner.nextInt();
                        scanner.nextLine();
                        renderLogTable(logDao.getLogsByUserId(targetId));
                    } else {
                        System.out.println("Invalid User ID input!");
                        scanner.nextLine();
                    }
                }
                case 3 -> approvePendingUsers(scanner);
                case 4 -> {
                    logDao.logAction(1, "ADMIN LOGOUT", "Administrator logged out.");
                    System.out.println("\nLogging out...");
                    return;
                }
                default -> System.out.println("Invalid option. Try again.");
            }
        }
    }

    private void approvePendingUsers(Scanner scanner) {
        System.out.println("\n==========================================");
        System.out.println("          PENDING USER ACCOUNTS           ");
        System.out.println("==========================================");

        String selectSql = "SELECT user_id, username, status FROM users WHERE status = 'PENDING'";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(selectSql)) {

            boolean hasPending = false;
            System.out.printf("%-8s | %-20s | %-10s%n", "USER ID", "USERNAME", "STATUS");
            System.out.println("------------------------------------------");
            while (rs.next()) {
                hasPending = true;
                System.out.printf("%-8d | %-20s | %-10s%n",
                        rs.getInt("user_id"),
                        rs.getString("username"),
                        rs.getString("status"));
            }
            System.out.println("------------------------------------------");

            if (!hasPending) {
                System.out.println("No pending user accounts to approve.");
                return;
            }

            System.out.print("Enter User ID to approve (or 0 to go back): ");
            if (scanner.hasNextInt()) {
                int approveId = scanner.nextInt();
                scanner.nextLine();
                if (approveId == 0) return;

                String updateSql = "UPDATE users SET status = 'APPROVED' WHERE user_id = ? AND status = 'PENDING'";
                try (PreparedStatement pstmt = conn.prepareStatement(updateSql)) {
                    pstmt.setInt(1, approveId);
                    int rows = pstmt.executeUpdate();
                    if (rows > 0) {
                        System.out.println("\nSUCCESS: User ID " + approveId + " status updated to APPROVED.");
                        logDao.logAction(1, "USER_APPROVED", "Admin [1] approved account for User ID " + approveId);
                    } else {
                        System.out.println("Invalid User ID or user is already approved.");
                    }
                }
            } else {
                scanner.nextLine();
            }
        } catch (SQLException e) {
            System.err.println("Error managing pending users: " + e.getMessage());
        }
    }

    private void renderLogTable(List<ActivityLog> logs) {
        if (logs.isEmpty()) {
            System.out.println("No activity logs recorded.");
            return;
        }

        System.out.println("========================================================================================================================");
        System.out.printf("%-7s | %-8s | %-15s | %-55s | %-20s%n", "LOG ID", "USER ID", "ACTION", "DESCRIPTION", "TIMESTAMP");
        System.out.println("========================================================================================================================");

        for (ActivityLog log : logs) {
            System.out.printf("%-7d | %-8d | %-15s | %-55s | %-20s%n",
                    log.getLogId(),
                    log.getUserId(),
                    log.getActionType(),
                    log.getDescription(),
                    log.getTimestamp().toString());
        }
        System.out.println("========================================================================================================================");
    }
}