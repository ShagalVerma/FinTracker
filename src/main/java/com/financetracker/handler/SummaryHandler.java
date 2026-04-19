package com.financetracker.handler;

import com.financetracker.facade.FinanceFacade;
import com.financetracker.util.JsonUtil;
import com.sun.net.httpserver.HttpExchange;

import java.time.LocalDate;
import java.util.Map;

/**
 * Handles:
 *   GET /api/summary?month=YYYY-MM   → monthly totals + category breakdown
 */
public class SummaryHandler extends BaseHandler {

    private final FinanceFacade facade = new FinanceFacade();

    @Override
    protected void handleRequest(HttpExchange exchange) throws Exception {
        if (!"GET".equals(exchange.getRequestMethod())) {
            sendJson(exchange, 405, JsonUtil.error("Method not allowed.")); return;
        }
        String token = getToken(exchange);
        if (!authProxy.isAuthenticated(token)) { unauthorized(exchange); return; }
        int userId = authProxy.getUserId(token);

        Map<String, String> params = queryParams(exchange);
        String month = params.getOrDefault("month", LocalDate.now().toString().substring(0, 7));

        Map<String, Object> summary = facade.getMonthlySummary(userId, month);
        sendJson(exchange, 200, JsonUtil.toJson(summary));
    }
}
