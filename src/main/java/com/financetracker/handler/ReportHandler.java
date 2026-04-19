package com.financetracker.handler;

import com.financetracker.builder.Report;
import com.financetracker.facade.FinanceFacade;
import com.financetracker.util.JsonUtil;
import com.sun.net.httpserver.HttpExchange;

import java.time.LocalDate;
import java.util.Map;

/**
 * Handles:
 *   GET /api/report?from=YYYY-MM-DD&to=YYYY-MM-DD&format=csv|json
 *
 * Builds a Report using the Builder pattern (via FinanceFacade) and returns either
 * a downloadable CSV file or a JSON summary depending on the format parameter.
 */
public class ReportHandler extends BaseHandler {

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
        String today = LocalDate.now().toString();
        String from  = params.getOrDefault("from", today.substring(0, 7) + "-01");
        String to    = params.getOrDefault("to", today);
        String fmt   = params.getOrDefault("format", "json").toLowerCase();

        // Build the report using the Builder pattern via facade
        Report report = facade.buildReport(userId, from, to);

        if ("csv".equals(fmt)) {
            String csv      = facade.reportToCsv(report);
            String filename = "report_" + from + "_to_" + to + ".csv";
            sendCsv(exchange, csv, filename);
        } else {
            // Return a JSON summary (omit raw transaction rows for brevity)
            Map<String, Object> json = Map.of(
                "title",             report.getTitle(),
                "username",          report.getUsername(),
                "dateRange",         report.getDateRange(),
                "totalIncome",       report.getTotalIncome(),
                "totalExpense",      report.getTotalExpense(),
                "netBalance",        report.getNetBalance(),
                "categoryBreakdown", report.getCategoryBreakdown(),
                "generatedAt",       report.getGeneratedAt()
            );
            sendJson(exchange, 200, JsonUtil.toJson(json));
        }
    }
}
