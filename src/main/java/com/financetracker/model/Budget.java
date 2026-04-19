package com.financetracker.model;

public class Budget {
    private int id;
    private int userId;
    private String category;
    private double limitAmount;
    private String month;   // e.g. "2026-04"
    private double spent;   // calculated field (not stored in DB)

    public Budget() {}

    public Budget(int id, int userId, String category, double limitAmount, String month) {
        this.id = id;
        this.userId = userId;
        this.category = category;
        this.limitAmount = limitAmount;
        this.month = month;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public double getLimitAmount() { return limitAmount; }
    public void setLimitAmount(double limitAmount) { this.limitAmount = limitAmount; }

    public String getMonth() { return month; }
    public void setMonth(String month) { this.month = month; }

    public double getSpent() { return spent; }
    public void setSpent(double spent) { this.spent = spent; }
}
