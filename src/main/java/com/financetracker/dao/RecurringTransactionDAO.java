package com.financetracker.dao;

import com.financetracker.model.RecurringTransaction;
import java.sql.*;
import java.time.LocalDate;
import java.util.*;

public class RecurringTransactionDAO {

    private Connection conn() {
        return DatabaseConnection.getInstance().getConnection();
    }

    public RecurringTransaction add(RecurringTransaction rt) throws SQLException {
        String sql = """
            INSERT INTO recurring_transactions (user_id, type, category, amount, description, frequency, next_date)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """;
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, rt.getUserId());
            ps.setString(2, rt.getType());
            ps.setString(3, rt.getCategory());
            ps.setDouble(4, rt.getAmount());
            ps.setString(5, rt.getDescription());
            ps.setString(6, rt.getFrequency());
            ps.setString(7, rt.getNextDate());
            ps.executeUpdate();
        }
        try (Statement st = conn().createStatement();
             ResultSet rs = st.executeQuery("SELECT last_insert_rowid()")) {
            if (rs.next()) rt.setId(rs.getInt(1));
        }
        return rt;
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM recurring_transactions WHERE id = ?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public List<RecurringTransaction> findByUserId(int userId) throws SQLException {
        String sql = "SELECT * FROM recurring_transactions WHERE user_id = ? ORDER BY next_date ASC";
        List<RecurringTransaction> list = new ArrayList<>();
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }

    /** Returns all recurring transactions that are due (next_date <= today). */
    public List<RecurringTransaction> findDue(int userId) throws SQLException {
        String today = LocalDate.now().toString();
        String sql = "SELECT * FROM recurring_transactions WHERE user_id = ? AND next_date <= ?";
        List<RecurringTransaction> list = new ArrayList<>();
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, today);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }

    /** Advances the next_date based on frequency. */
    public void advanceNextDate(int id, String frequency, String currentNextDate) throws SQLException {
        LocalDate next = LocalDate.parse(currentNextDate);
        switch (frequency) {
            case "DAILY"   -> next = next.plusDays(1);
            case "WEEKLY"  -> next = next.plusWeeks(1);
            case "MONTHLY" -> next = next.plusMonths(1);
        }
        String sql = "UPDATE recurring_transactions SET next_date = ? WHERE id = ?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, next.toString());
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }

    private RecurringTransaction map(ResultSet rs) throws SQLException {
        return new RecurringTransaction(
            rs.getInt("id"),
            rs.getInt("user_id"),
            rs.getString("type"),
            rs.getString("category"),
            rs.getDouble("amount"),
            rs.getString("description"),
            rs.getString("frequency"),
            rs.getString("next_date")
        );
    }
}
