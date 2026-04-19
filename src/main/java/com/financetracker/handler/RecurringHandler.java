package com.financetracker.handler;

import com.financetracker.facade.FinanceFacade;
import com.financetracker.model.RecurringTransaction;
import com.financetracker.util.JsonUtil;
import com.sun.net.httpserver.HttpExchange;

import java.util.List;
import java.util.Map;

/**
 * Handles:
 *   GET    /api/recurring          → list recurring transactions
 *   POST   /api/recurring          → create a recurring transaction
 *   DELETE /api/recurring/{id}     → delete a recurring transaction
 */
public class RecurringHandler extends BaseHandler {

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
            case "POST"   -> add(exchange, userId);
            case "DELETE" -> {
                if (id < 0) { badRequest(exchange, "Recurring transaction ID required."); return; }
                delete(exchange, id);
            }
            default       -> sendJson(exchange, 405, JsonUtil.error("Method not allowed."));
        }
    }

    private void getAll(HttpExchange exchange, int userId) throws Exception {
        List<RecurringTransaction> list = facade.getRecurring(userId);
        sendJson(exchange, 200, JsonUtil.toJson(list));
    }

    private void add(HttpExchange exchange, int userId) throws Exception {
        Map<String, Object> body = JsonUtil.parseBody(readBody(exchange));

        String type        = str(body, "type");
        String category    = str(body, "category");
        double amount      = toDouble(body.get("amount"));
        String description = str(body, "description");
        String frequency   = str(body, "frequency");
        String nextDate    = str(body, "nextDate");

        if (type == null || category == null || frequency == null || nextDate == null) {
            badRequest(exchange, "type, category, frequency and nextDate are required."); return;
        }

        RecurringTransaction rt = new RecurringTransaction();
        rt.setUserId(userId);
        rt.setType(type.toUpperCase());
        rt.setCategory(category);
        rt.setAmount(amount);
        rt.setDescription(description == null ? "" : description);
        rt.setFrequency(frequency.toUpperCase());
        rt.setNextDate(nextDate);

        RecurringTransaction saved = facade.addRecurring(rt);
        sendJson(exchange, 201, JsonUtil.toJson(saved));
    }

    private void delete(HttpExchange exchange, int id) throws Exception {
        facade.deleteRecurring(id);
        sendJson(exchange, 200, JsonUtil.message("Recurring transaction deleted."));
    }

    private String str(Map<String, Object> map, String key) {
        Object v = map.get(key);
        return v == null ? null : v.toString();
    }

    private double toDouble(Object v) {
        if (v == null) throw new IllegalArgumentException("amount is required.");
        return Double.parseDouble(v.toString());
    }
}
