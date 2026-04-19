package com.financetracker.handler;

import com.financetracker.facade.FinanceFacade;
import com.financetracker.model.Transaction;
import com.financetracker.util.JsonUtil;
import com.sun.net.httpserver.HttpExchange;

import java.util.List;
import java.util.Map;

/**
 * Handles:
 *   GET    /api/transactions          → list all for authenticated user
 *   POST   /api/transactions          → add a new transaction
 *   DELETE /api/transactions/{id}     → delete a transaction
 *
 * Uses AuthProxy (via BaseHandler.getToken + authProxy) to protect every endpoint.
 * Delegates all business logic to FinanceFacade.
 */
public class TransactionHandler extends BaseHandler {

    private final FinanceFacade facade = new FinanceFacade();

    @Override
    protected void handleRequest(HttpExchange exchange) throws Exception {
        String token = getToken(exchange);
        if (!authProxy.isAuthenticated(token)) { unauthorized(exchange); return; }
        int userId = authProxy.getUserId(token);

        String method = exchange.getRequestMethod();
        int    id     = pathId(exchange);   // -1 if no ID in path

        switch (method) {
            case "GET"    -> getAll(exchange, userId);
            case "POST"   -> add(exchange, userId);
            case "DELETE" -> {
                if (id < 0) { badRequest(exchange, "Transaction ID required."); return; }
                delete(exchange, id);
            }
            default       -> sendJson(exchange, 405, JsonUtil.error("Method not allowed."));
        }
    }

    private void getAll(HttpExchange exchange, int userId) throws Exception {
        List<Transaction> list = facade.getTransactions(userId);
        sendJson(exchange, 200, JsonUtil.toJson(list));
    }

    private void add(HttpExchange exchange, int userId) throws Exception {
        Map<String, Object> body = JsonUtil.parseBody(readBody(exchange));
        try {
            String type        = str(body, "type");
            String category    = str(body, "category");
            double amount      = toDouble(body.get("amount"));
            String description = str(body, "description");
            String date        = str(body, "date");

            Transaction t = facade.addTransaction(type, userId, category, amount, description, date);
            sendJson(exchange, 201, JsonUtil.toJson(t));
        } catch (IllegalArgumentException e) {
            badRequest(exchange, e.getMessage());
        }
    }

    private void delete(HttpExchange exchange, int id) throws Exception {
        facade.deleteTransaction(id);
        sendJson(exchange, 200, JsonUtil.message("Transaction deleted."));
    }

    private String str(Map<String, Object> map, String key) {
        Object v = map.get(key);
        return v == null ? null : v.toString();
    }

    private double toDouble(Object v) {
        if (v == null) throw new IllegalArgumentException("Amount is required.");
        return Double.parseDouble(v.toString());
    }
}
