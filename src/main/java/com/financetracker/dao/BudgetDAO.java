package com.financetracker.dao;

import com.financetracker.model.Budget;
import java.sql.*;
import java.util.*;

public class BudgetDAO {

    private Connection conn() {
        return DatabaseConnection.getInstance().getConnection();
    }

    /** Inserts or updates a budget for (user, category, month). Returns the saved budget. */
    public Budget addOrUpdate(int userId, String category, double limitAmount, String month) throws SQLException {
        String sql = """
            INSERT INTO budgets (user_id, category, limit_amount, month)
            VALUES (?, ?, ?, ?)
            ON CONFLICT(user_id, category, month) DO UPDATE SET limit_amount = excluded.limit_amount
            """;
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, category);
            ps.setDouble(3, limitAmount);
            ps.setString(4, month);
            ps.executeUpdate();
        }
        return findByUserCategoryMonth(userId, category, month);
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM budgets WHERE id = ?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public List<Budget> findByUserId(int userId) throws SQLException {
        String sql = "SELECT * FROM budgets WHERE user_id = ? ORDER BY month DESC, category ASC";
        List<Budget> list = new ArrayList<>();
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }

    private Budget findByUserCategoryMonth(int userId, String category, String month) throws SQLException {
        String sql = "SELECT * FROM budgets WHERE user_id = ? AND category = ? AND month = ?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, category);
            ps.setString(3, month);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return map(rs);
            }
        }
        return null;
    }

    private Budget map(ResultSet rs) throws SQLException {
        return new Budget(
            rs.getInt("id"),
            rs.getInt("user_id"),
            rs.getString("category"),
            rs.getDouble("limit_amount"),
            rs.getString("month")
        );
    }
}
