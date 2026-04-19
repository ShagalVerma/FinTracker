package com.financetracker.proxy;

import com.financetracker.dao.UserDAO;

/**
 * DESIGN PATTERN: Proxy (Protection Proxy)
 *
 * Acts as a gatekeeper in front of the application's services.
 * Every HTTP handler calls AuthProxy.requireAuth(token) before performing
 * any operation.  If the token is invalid or missing, the proxy rejects the
 * request — the real service never executes.
 *
 * This separates the authentication concern from the business logic,
 * following the Single Responsibility Principle.
 */
public class AuthProxy {

    private final UserDAO userDAO;

    public AuthProxy() {
        this.userDAO = new UserDAO();
    }

    /**
     * Returns true if the session token is valid.
     */
    public boolean isAuthenticated(String token) {
        if (token == null || token.isBlank()) return false;
        try {
            return userDAO.validateSession(token);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Resolves the userId from a valid token, or -1 if token is invalid.
     */
    public int getUserId(String token) {
        if (token == null || token.isBlank()) return -1;
        try {
            return userDAO.getUserIdFromToken(token);
        } catch (Exception e) {
            return -1;
        }
    }
}
