package com.financetracker.dao;

import com.financetracker.model.User;
import java.sql.*;
import java.util.UUID;

public class UserDAO {

    private Connection conn() {
        return DatabaseConnection.getInstance().getConnection();
    }

    /** Register a new user. Returns the created User or null on duplicate. */
    public User create(String username, String email, String passwordHash, String salt) throws SQLException {
        String sql = "INSERT INTO users (username, email, password_hash, salt) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, email);
            ps.setString(3, passwordHash);
            ps.setString(4, salt);
            ps.executeUpdate();
        }
        try (Statement st = conn().createStatement();
             ResultSet rs = st.executeQuery("SELECT last_insert_rowid()")) {
            if (rs.next()) return findById(rs.getInt(1));
        }
        return null;
    }

    public User findByUsername(String username) throws SQLException {
        String sql = "SELECT * FROM users WHERE username = ?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return map(rs);
            }
        }
        return null;
    }

    public User findById(int id) throws SQLException {
        String sql = "SELECT * FROM users WHERE id = ?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return map(rs);
            }
        }
        return null;
    }

    /** Creates a session and returns the generated token. */
    public String createSession(int userId) throws SQLException {
        String token = UUID.randomUUID().toString();
        String sql = "INSERT INTO sessions (user_id, token) VALUES (?, ?)";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, token);
            ps.executeUpdate();
        }
        return token;
    }

    /** Deletes a session (logout). */
    public void deleteSession(String token) throws SQLException {
        String sql = "DELETE FROM sessions WHERE token = ?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, token);
            ps.executeUpdate();
        }
    }

    /** Checks whether a token is valid. */
    public boolean validateSession(String token) throws SQLException {
        String sql = "SELECT id FROM sessions WHERE token = ?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, token);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    /** Returns the userId associated with a session token, or -1 if invalid. */
    public int getUserIdFromToken(String token) throws SQLException {
        String sql = "SELECT user_id FROM sessions WHERE token = ?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, token);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("user_id");
            }
        }
        return -1;
    }

    private User map(ResultSet rs) throws SQLException {
        return new User(
            rs.getInt("id"),
            rs.getString("username"),
            rs.getString("email"),
            rs.getString("password_hash"),
            rs.getString("salt"),
            rs.getString("created_at")
        );
    }
}
