package com.inventory.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static final String URL = "jdbc:sqlite:computer_parts_inventory.db";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL);
    }
}