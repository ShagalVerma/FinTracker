package com.financetracker.builder;

import java.util.List;
import java.util.Map;

/**
 * DESIGN PATTERN: Builder
 *
 * A financial Report has many optional fields (date range, category breakdown,
 * transaction list, format, etc.).  Using a Builder avoids telescoping constructors
 * and makes report construction readable and step-by-step.
 *
 * Usage:
 *   Report r = new Report.Builder()
 *       .title("Monthly Report")
 *       .totalIncome(5000)
 *       .totalExpense(3200)
 *       .netBalance(1800)
 *       .categoryBreakdown(map)
 *       .build();
 */
public class Report {

    private final String title;
    private final String username;
    private final String dateRange;
    private final double totalIncome;
    private final double totalExpense;
    private final double netBalance;
    private final Map<String, Double> categoryBreakdown;
    private final List<String[]> transactions;   // each row: [date, type, category, amount, description]
    private final String format;
    private final String generatedAt;

    // Private constructor — only the inner Builder may call this
    private Report(Builder b) {
        this.title             = b.title;
        this.username          = b.username;
        this.dateRange         = b.dateRange;
        this.totalIncome       = b.totalIncome;
        this.totalExpense      = b.totalExpense;
        this.netBalance        = b.netBalance;
        this.categoryBreakdown = b.categoryBreakdown;
        this.transactions      = b.transactions;
        this.format            = b.format;
        this.generatedAt       = b.generatedAt;
    }

    // ── Getters ──────────────────────────────────────────────────────────────

    public String getTitle()                        { return title; }
    public String getUsername()                     { return username; }
    public String getDateRange()                    { return dateRange; }
    public double getTotalIncome()                  { return totalIncome; }
    public double getTotalExpense()                 { return totalExpense; }
    public double getNetBalance()                   { return netBalance; }
    public Map<String, Double> getCategoryBreakdown(){ return categoryBreakdown; }
    public List<String[]> getTransactions()         { return transactions; }
    public String getFormat()                       { return format; }
    public String getGeneratedAt()                  { return generatedAt; }

    // ── Builder ──────────────────────────────────────────────────────────────

    public static class Builder {
        // Required
        private String title = "Finance Report";
        // Optional with sensible defaults
        private String username    = "";
        private String dateRange   = "";
        private double totalIncome  = 0;
        private double totalExpense = 0;
        private double netBalance   = 0;
        private Map<String, Double> categoryBreakdown = Map.of();
        private List<String[]> transactions = List.of();
        private String format      = "JSON";
        private String generatedAt = java.time.LocalDateTime.now().toString();

        public Builder title(String title)                         { this.title = title; return this; }
        public Builder username(String username)                   { this.username = username; return this; }
        public Builder dateRange(String dateRange)                 { this.dateRange = dateRange; return this; }
        public Builder totalIncome(double v)                       { this.totalIncome = v; return this; }
        public Builder totalExpense(double v)                      { this.totalExpense = v; return this; }
        public Builder netBalance(double v)                        { this.netBalance = v; return this; }
        public Builder categoryBreakdown(Map<String, Double> map)  { this.categoryBreakdown = map; return this; }
        public Builder transactions(List<String[]> txns)           { this.transactions = txns; return this; }
        public Builder format(String format)                       { this.format = format; return this; }
        public Builder generatedAt(String ts)                      { this.generatedAt = ts; return this; }

        public Report build() {
            return new Report(this);
        }
    }
}
