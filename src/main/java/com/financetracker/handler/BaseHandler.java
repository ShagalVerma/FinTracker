package com.financetracker.handler;

import com.financetracker.proxy.AuthProxy;
import com.financetracker.util.JsonUtil;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Abstract base for all HTTP handlers.
 * Provides CORS, response helpers, body parsing, and auth token extraction.
 * Concrete handlers extend this and override handle().
 */
public abstract class BaseHandler implements HttpHandler {

    protected final AuthProxy authProxy = new AuthProxy();

    @Override
    public final void handle(HttpExchange exchange) throws IOException {
        // Handle preflight CORS requests from the React frontend
        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            setCorsHeaders(exchange);
            exchange.sendResponseHeaders(204, -1);
            return;
        }
        setCorsHeaders(exchange);
        try {
            handleRequest(exchange);
        } catch (Exception e) {
            System.err.println("[Handler Error] " + e.getMessage());
            sendJson(exchange, 500, JsonUtil.error("Internal server error: " + e.getMessage()));
        }
    }

    protected abstract void handleRequest(HttpExchange exchange) throws Exception;

    // ── Response helpers ─────────────────────────────────────────────────────

    protected void sendJson(HttpExchange exchange, int status, String json) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    protected void sendCsv(HttpExchange exchange, String csv, String filename) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "text/csv; charset=UTF-8");
        exchange.getResponseHeaders().set("Content-Disposition", "attachment; filename=\"" + filename + "\"");
        byte[] bytes = csv.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(200, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    protected void unauthorized(HttpExchange exchange) throws IOException {
        sendJson(exchange, 401, JsonUtil.error("Unauthorized. Please log in."));
    }

    protected void notFound(HttpExchange exchange) throws IOException {
        sendJson(exchange, 404, JsonUtil.error("Not found."));
    }

    protected void badRequest(HttpExchange exchange, String msg) throws IOException {
        sendJson(exchange, 400, JsonUtil.error(msg));
    }

    // ── Request helpers ──────────────────────────────────────────────────────

    protected String readBody(HttpExchange exchange) throws IOException {
        return new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
    }

    /** Extracts Bearer token from the Authorization header. */
    protected String getToken(HttpExchange exchange) {
        String auth = exchange.getRequestHeaders().getFirst("Authorization");
        if (auth != null && auth.startsWith("Bearer ")) {
            return auth.substring(7).trim();
        }
        return null;
    }

    /** Parses ?key=value&... query string into a Map. */
    protected Map<String, String> queryParams(HttpExchange exchange) {
        Map<String, String> params = new HashMap<>();
        String query = exchange.getRequestURI().getQuery();
        if (query != null) {
            for (String pair : query.split("&")) {
                String[] kv = pair.split("=", 2);
                if (kv.length == 2) params.put(kv[0], kv[1]);
            }
        }
        return params;
    }

    /**
     * Extracts the last path segment as an integer ID.
     * E.g. /api/transactions/42  →  42
     * Returns -1 if not present or not parseable.
     */
    protected int pathId(HttpExchange exchange) {
        String[] parts = exchange.getRequestURI().getPath().split("/");
        try {
            return Integer.parseInt(parts[parts.length - 1]);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    // ── CORS ─────────────────────────────────────────────────────────────────

    private void setCorsHeaders(HttpExchange exchange) {
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin",  "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Authorization");
    }
}
