package com.financetracker.model;

public class RecurringTransaction {
    private int id;
    private int userId;
    private String type;        // "INCOME" or "EXPENSE"
    private String category;
    private double amount;
    private String description;
    private String frequency;   // "DAILY", "WEEKLY", "MONTHLY"
    private String nextDate;    // ISO format: "2026-04-19"

    public RecurringTransaction() {}

    public RecurringTransaction(int id, int userId, String type, String category,
                                double amount, String description, String frequency, String nextDate) {
        this.id = id;
        this.userId = userId;
        this.type = type;
        this.category = category;
        this.amount = amount;
        this.description = description;
        this.frequency = frequency;
        this.nextDate = nextDate;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getFrequency() { return frequency; }
    public void setFrequency(String frequency) { this.frequency = frequency; }

    public String getNextDate() { return nextDate; }
    public void setNextDate(String nextDate) { this.nextDate = nextDate; }
}
