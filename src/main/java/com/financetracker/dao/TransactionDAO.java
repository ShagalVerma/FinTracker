package com.financetracker.dao;

import com.financetracker.model.Transaction;
import java.sql.*;
import java.util.*;

public class TransactionDAO {

    private Connection conn() {
        return DatabaseConnection.getInstance().getConnection();
    }

    /** Inserts a transaction and returns its generated ID. */
    public int add(Transaction t) throws SQLException {
        String sql = "INSERT INTO transactions (user_id, type, category, amount, description, date) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, t.getUserId());
            ps.setString(2, t.getType());
            ps.setString(3, t.getCategory());
            ps.setDouble(4, t.getAmount());
            ps.setString(5, t.getDescription());
            ps.setString(6, t.getDate());
            ps.executeUpdate();
        }
        try (Statement st = conn().createStatement();
             ResultSet rs = st.executeQuery("SELECT last_insert_rowid()")) {
            if (rs.next()) return rs.getInt(1);
        }
        return -1;
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM transactions WHERE id = ?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public Transaction findById(int id) throws SQLException {
        String sql = "SELECT * FROM transactions WHERE id = ?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return map(rs);
            }
        }
        return null;
    }

    public List<Transaction> findByUserId(int userId) throws SQLException {
        String sql = "SELECT * FROM transactions WHERE user_id = ? ORDER BY date DESC, created_at DESC";
        List<Transaction> list = new ArrayList<>();
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }

    /** Returns transactions for a user within a date range (inclusive). */
    public List<Transaction> findByUserIdAndDateRange(int userId, String from, String to) throws SQLException {
        String sql = "SELECT * FROM transactions WHERE user_id = ? AND date >= ? AND date <= ? ORDER BY date DESC";
        List<Transaction> list = new ArrayList<>();
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, from);
            ps.setString(3, to);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }

    /** Returns total spending per category for a given month (YYYY-MM). Only EXPENSE type. */
    public Map<String, Double> getCategorySpending(int userId, String month) throws SQLException {
        String sql = """
            SELECT category, SUM(amount) as total
            FROM transactions
            WHERE user_id = ? AND type = 'EXPENSE' AND strftime('%Y-%m', date) = ?
            GROUP BY category
            """;
        Map<String, Double> map = new LinkedHashMap<>();
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, month);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) map.put(rs.getString("category"), rs.getDouble("total"));
            }
        }
        return map;
    }

    private Transaction map(ResultSet rs) throws SQLException {
        return new Transaction(
            rs.getInt("id"),
            rs.getInt("user_id"),
            rs.getString("type"),
            rs.getString("category"),
            rs.getDouble("amount"),
            rs.getString("description"),
            rs.getString("date"),
            rs.getString("created_at")
        );
    }
}
