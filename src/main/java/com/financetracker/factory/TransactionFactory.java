package com.financetracker.factory;

import com.financetracker.model.Transaction;

/**
 * DESIGN PATTERN: Factory
 *
 * Centralises the creation of Transaction objects.
 * The caller specifies the type ("INCOME" or "EXPENSE") and the factory
 * validates the input and constructs the correct object.
 * This decouples object creation from the rest of the application; if
 * new transaction types are added in the future, only this class changes.
 */
public class TransactionFactory {

    /**
     * Creates a transaction of the given type.
     *
     * @param type        "INCOME" or "EXPENSE"
     * @param userId      owner of the transaction
     * @param category    e.g. "Food", "Salary"
     * @param amount      positive monetary value
     * @param description optional free-text note
     * @param date        ISO date string "YYYY-MM-DD"
     * @return a fully-initialised (but not yet persisted) Transaction
     */
    public static Transaction create(String type, int userId, String category,
                                     double amount, String description, String date) {
        if (type == null || (!type.equalsIgnoreCase("INCOME") && !type.equalsIgnoreCase("EXPENSE"))) {
            throw new IllegalArgumentException("Transaction type must be INCOME or EXPENSE, got: " + type);
        }
        if (amount <= 0) {
            throw new IllegalArgumentException("Transaction amount must be positive.");
        }
        if (category == null || category.isBlank()) {
            throw new IllegalArgumentException("Category must not be blank.");
        }
        if (date == null || date.isBlank()) {
            throw new IllegalArgumentException("Date must not be blank.");
        }

        Transaction t = new Transaction();
        t.setType(type.toUpperCase());
        t.setUserId(userId);
        t.setCategory(category.trim());
        t.setAmount(amount);
        t.setDescription(description == null ? "" : description.trim());
        t.setDate(date);
        return t;
    }

    /** Convenience method for income transactions. */
    public static Transaction createIncome(int userId, String category,
                                           double amount, String description, String date) {
        return create("INCOME", userId, category, amount, description, date);
    }

    /** Convenience method for expense transactions. */
    public static Transaction createExpense(int userId, String category,
                                            double amount, String description, String date) {
        return create("EXPENSE", userId, category, amount, description, date);
    }
}
