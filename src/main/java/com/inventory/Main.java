package com.inventory;

import com.inventory.dao.DatabaseInitializer;
import com.inventory.ui.Authentication;

public class Main {
    public static void main(String[] args) {
        DatabaseInitializer.initializeDatabase();
        Authentication auth = new Authentication();
        auth.start();
    }
}