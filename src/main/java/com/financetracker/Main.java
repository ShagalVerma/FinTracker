package com.financetracker;

import com.financetracker.dao.DatabaseConnection;
import com.financetracker.handler.*;
import com.sun.net.httpserver.HttpServer;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

/**
 * Application entry point.
 * Starts the embedded HTTP server on port 8080 and registers all route contexts.
 *
 * Build:  mvn package -q
 * Run:    java -jar target/finance-tracker-1.0-SNAPSHOT-jar-with-dependencies.jar
 * Then:   cd frontend && npm install && npm run dev
 */
public class Main {

    private static final int PORT = 8080;

    public static void main(String[] args) throws Exception {
        // Eagerly initialise the Singleton DB connection and schema
        DatabaseConnection.getInstance();

        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);

        // Register route contexts
        server.createContext("/api/auth/",        new AuthHandler());
        server.createContext("/api/transactions",  new TransactionHandler());
        server.createContext("/api/budgets",       new BudgetHandler());
        server.createContext("/api/summary",       new SummaryHandler());
        server.createContext("/api/report",        new ReportHandler());
        server.createContext("/api/recurring",     new RecurringHandler());

        // Thread pool for concurrent request handling
        server.setExecutor(Executors.newFixedThreadPool(10));
        server.start();

        System.out.println("╔══════════════════════════════════════════╗");
        System.out.println("║   Finance Tracker API running on :8080   ║");
        System.out.println("╚══════════════════════════════════════════╝");
        System.out.println("  → Start the React frontend with:");
        System.out.println("    cd frontend && npm install && npm run dev");
    }
}
