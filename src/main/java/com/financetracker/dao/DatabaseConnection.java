package com.financetracker.dao;

import java.sql.*;

/**
 * DESIGN PATTERN: Singleton
 *
 * Ensures only one database connection instance exists throughout the application.
 * This avoids resource waste and prevents conflicting concurrent connections to SQLite.
 * The getInstance() method is synchronized to make it thread-safe.
 */
public class DatabaseConnection {

    private static DatabaseConnection instance;
    private Connection connection;
    private static final String DB_URL = "jdbc:sqlite:finance_tracker.db";

    // Private constructor prevents external instantiation
    private DatabaseConnection() {
        try {
            connection = DriverManager.getConnection(DB_URL);
            // Enable foreign key enforcement
            try (Statement stmt = connection.createStatement()) {
                stmt.executeUpdate("PRAGMA foreign_keys=ON");
            }
            initializeSchema();
            System.out.println("[DB] Connected to SQLite database.");
        } catch (SQLException e) {
            throw new RuntimeException("Failed to connect to database: " + e.getMessage(), e);
        }
    }

    /**
     * Thread-safe access to the single instance.
     */
    public static synchronized DatabaseConnection getInstance() {
        if (instance == null || isConnectionClosed()) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    private static boolean isConnectionClosed() {
        try {
            return instance.connection == null || instance.connection.isClosed();
        } catch (SQLException e) {
            return true;
        }
    }

    public Connection getConnection() {
        return connection;
    }

    /**
     * Creates all tables if they do not already exist.
     */
    private void initializeSchema() throws SQLException {
        String[] ddl = {
            // Users table
            """
            CREATE TABLE IF NOT EXISTS users (
                id            INTEGER PRIMARY KEY AUTOINCREMENT,
                username      TEXT    UNIQUE NOT NULL,
                email         TEXT    UNIQUE NOT NULL,
                password_hash TEXT    NOT NULL,
                salt          TEXT    NOT NULL,
                created_at    TEXT    DEFAULT (datetime('now'))
            )
            """,
            // Sessions (for auth proxy)
            """
            CREATE TABLE IF NOT EXISTS sessions (
                id         INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id    INTEGER NOT NULL,
                token      TEXT    UNIQUE NOT NULL,
                created_at TEXT    DEFAULT (datetime('now')),
                FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
            )
            """,
            // Transactions
            """
            CREATE TABLE IF NOT EXISTS transactions (
                id          INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id     INTEGER NOT NULL,
                type        TEXT    NOT NULL CHECK (type IN ('INCOME','EXPENSE')),
                category    TEXT    NOT NULL,
                amount      REAL    NOT NULL,
                description TEXT,
                date        TEXT    NOT NULL,
                created_at  TEXT    DEFAULT (datetime('now')),
                FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
            )
            """,
            // Budgets
            """
            CREATE TABLE IF NOT EXISTS budgets (
                id           INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id      INTEGER NOT NULL,
                category     TEXT    NOT NULL,
                limit_amount REAL    NOT NULL,
                month        TEXT    NOT NULL,
                UNIQUE(user_id, category, month),
                FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
            )
            """,
            // Recurring transactions
            """
            CREATE TABLE IF NOT EXISTS recurring_transactions (
                id          INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id     INTEGER NOT NULL,
                type        TEXT    NOT NULL CHECK (type IN ('INCOME','EXPENSE')),
                category    TEXT    NOT NULL,
                amount      REAL    NOT NULL,
                description TEXT,
                frequency   TEXT    NOT NULL CHECK (frequency IN ('DAILY','WEEKLY','MONTHLY')),
                next_date   TEXT    NOT NULL,
                FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
            )
            """
        };

        try (Statement stmt = connection.createStatement()) {
            for (String sql : ddl) {
                stmt.execute(sql);
            }
        }
        System.out.println("[DB] Schema initialized.");
    }
}
