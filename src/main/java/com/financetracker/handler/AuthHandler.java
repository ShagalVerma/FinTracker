package com.financetracker.handler;

import com.financetracker.dao.UserDAO;
import com.financetracker.model.User;
import com.financetracker.util.JsonUtil;
import com.sun.net.httpserver.HttpExchange;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.UUID;

/**
 * Handles:
 *   POST /api/auth/signup
 *   POST /api/auth/login
 *   POST /api/auth/logout
 */
public class AuthHandler extends BaseHandler {

    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void handleRequest(HttpExchange exchange) throws Exception {
        String path   = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();

        if ("POST".equals(method) && path.endsWith("/signup")) {
            signup(exchange);
        } else if ("POST".equals(method) && path.endsWith("/login")) {
            login(exchange);
        } else if ("POST".equals(method) && path.endsWith("/logout")) {
            logout(exchange);
        } else {
            notFound(exchange);
        }
    }

    private void signup(HttpExchange exchange) throws Exception {
        Map<String, Object> body = JsonUtil.parseBody(readBody(exchange));
        String username = str(body, "username");
        String email    = str(body, "email");
        String password = str(body, "password");

        if (username == null || email == null || password == null) {
            badRequest(exchange, "username, email and password are required.");
            return;
        }
        if (password.length() < 6) {
            badRequest(exchange, "Password must be at least 6 characters.");
            return;
        }

        String salt         = UUID.randomUUID().toString();
        String passwordHash = hash(salt + password);

        try {
            User user = userDAO.create(username.trim(), email.trim(), passwordHash, salt);
            if (user == null) { badRequest(exchange, "Could not create user."); return; }

            String token = userDAO.createSession(user.getId());
            String resp  = JsonUtil.toJson(Map.of(
                "token",    token,
                "userId",   user.getId(),
                "username", user.getUsername(),
                "email",    user.getEmail()
            ));
            sendJson(exchange, 201, resp);
        } catch (Exception e) {
            if (e.getMessage() != null && e.getMessage().contains("UNIQUE")) {
                badRequest(exchange, "Username or email already exists.");
            } else {
                throw e;
            }
        }
    }

    private void login(HttpExchange exchange) throws Exception {
        Map<String, Object> body = JsonUtil.parseBody(readBody(exchange));
        String username = str(body, "username");
        String password = str(body, "password");

        if (username == null || password == null) {
            badRequest(exchange, "username and password are required.");
            return;
        }

        User user = userDAO.findByUsername(username.trim());
        if (user == null) {
            sendJson(exchange, 401, JsonUtil.error("Invalid username or password."));
            return;
        }

        String hashed = hash(user.getSalt() + password);
        if (!hashed.equals(user.getPasswordHash())) {
            sendJson(exchange, 401, JsonUtil.error("Invalid username or password."));
            return;
        }

        String token = userDAO.createSession(user.getId());
        String resp  = JsonUtil.toJson(Map.of(
            "token",    token,
            "userId",   user.getId(),
            "username", user.getUsername(),
            "email",    user.getEmail()
        ));
        sendJson(exchange, 200, resp);
    }

    private void logout(HttpExchange exchange) throws Exception {
        String token = getToken(exchange);
        if (token != null) userDAO.deleteSession(token);
        sendJson(exchange, 200, JsonUtil.message("Logged out successfully."));
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private String str(Map<String, Object> map, String key) {
        Object v = map.get(key);
        return v == null ? null : v.toString();
    }

    private String hash(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] bytes = md.digest(input.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
