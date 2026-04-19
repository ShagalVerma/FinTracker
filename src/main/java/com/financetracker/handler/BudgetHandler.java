package com.financetracker.handler;

import com.financetracker.facade.FinanceFacade;
import com.financetracker.model.Budget;
import com.financetracker.util.JsonUtil;
import com.sun.net.httpserver.HttpExchange;

import java.util.List;
import java.util.Map;

/**
 * Handles:
 *   GET    /api/budgets          → list budgets (with live 'spent' amounts)
 *   POST   /api/budgets          → create or update a budget
 *   DELETE /api/budgets/{id}     → delete a budget
 */
public class BudgetHandler extends BaseHandler {

    private final FinanceFacade facade = new FinanceFacade();

    @Override
    protected void handleRequest(HttpExchange exchange) throws Exception {
        String token = getToken(exchange);
        if (!authProxy.isAuthenticated(token)) { unauthorized(exchange); return; }
        int userId = authProxy.getUserId(token);

        String method = exchange.getRequestMethod();
        int    id     = pathId(exchange);

        switch (method) {
            case "GET"    -> getAll(exchange, userId);
            case "POST"   -> upsert(exchange, userId);
            case "DELETE" -> {
                if (id < 0) { badRequest(exchange, "Budget ID required."); return; }
                delete(exchange, id);
            }
            default       -> sendJson(exchange, 405, JsonUtil.error("Method not allowed."));
        }
    }

    private void getAll(HttpExchange exchange, int userId) throws Exception {
        List<Budget> budgets = facade.getBudgets(userId);
        sendJson(exchange, 200, JsonUtil.toJson(budgets));
    }

    private void upsert(HttpExchange exchange, int userId) throws Exception {
        Map<String, Object> body = JsonUtil.parseBody(readBody(exchange));
        String category = str(body, "category");
        String month    = str(body, "month");
        double limit    = toDouble(body.get("limitAmount"));

        if (category == null || month == null) {
            badRequest(exchange, "category and month are required.");
            return;
        }
        Budget b = facade.setBudget(userId, category, limit, month);
        sendJson(exchange, 201, JsonUtil.toJson(b));
    }

    private void delete(HttpExchange exchange, int id) throws Exception {
        facade.deleteBudget(id);
        sendJson(exchange, 200, JsonUtil.message("Budget deleted."));
    }

    private String str(Map<String, Object> map, String key) {
        Object v = map.get(key);
        return v == null ? null : v.toString();
    }

    private double toDouble(Object v) {
        if (v == null) throw new IllegalArgumentException("limitAmount is required.");
        return Double.parseDouble(v.toString());
    }
}
