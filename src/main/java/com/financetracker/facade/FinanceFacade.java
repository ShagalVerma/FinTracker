package com.financetracker.facade;

import com.financetracker.builder.Report;
import com.financetracker.command.*;
import com.financetracker.dao.*;
import com.financetracker.factory.TransactionFactory;
import com.financetracker.iterator.TransactionIterator;
import com.financetracker.model.*;

import java.time.LocalDate;
import java.util.*;

/**
 * DESIGN PATTERN: Facade
 *
 * Provides a single, simplified interface to the complex subsystem made up of
 * DAOs, the Factory, Builder, Command, and Iterator layers.
 *
 * HTTP handlers only call FinanceFacade — they are completely shielded from
 * the internal complexity of transaction creation (Factory), command execution
 * (Command), report construction (Builder), and collection traversal (Iterator).
 */
public class FinanceFacade {

    private final TransactionDAO       transactionDAO;
    private final BudgetDAO            budgetDAO;
    private final RecurringTransactionDAO recurringDAO;
    private final UserDAO              userDAO;

    public FinanceFacade() {
        this.transactionDAO = new TransactionDAO();
        this.budgetDAO      = new BudgetDAO();
        this.recurringDAO   = new RecurringTransactionDAO();
        this.userDAO        = new UserDAO();
    }

    // ── Transactions ─────────────────────────────────────────────────────────

    /**
     * Creates and persists a new transaction using Factory + Command patterns.
     */
    public Transaction addTransaction(String type, int userId, String category,
                                      double amount, String description, String date) throws Exception {
        // Factory creates the object with validation
        Transaction t = TransactionFactory.create(type, userId, category, amount, description, date);
        // Command encapsulates and executes the persistence operation
        AddTransactionCommand cmd = new AddTransactionCommand(transactionDAO, t);
        cmd.execute();
        return cmd.getTransaction();
    }

    /**
     * Deletes a transaction using the Command pattern (supports undo).
     */
    public void deleteTransaction(int transactionId) throws Exception {
        Command cmd = new DeleteTransactionCommand(transactionDAO, transactionId);
        cmd.execute();
    }

    /**
     * Returns all transactions for a user, processing any due recurring ones first.
     */
    public List<Transaction> getTransactions(int userId) throws Exception {
        processDueRecurring(userId);
        return transactionDAO.findByUserId(userId);
    }

    // ── Budgets ──────────────────────────────────────────────────────────────

    public Budget setBudget(int userId, String category, double limit, String month) throws Exception {
        return budgetDAO.addOrUpdate(userId, category, limit, month);
    }

    public void deleteBudget(int budgetId) throws Exception {
        budgetDAO.delete(budgetId);
    }

    /**
     * Returns budgets with real-time 'spent' amounts filled in.
     */
    public List<Budget> getBudgets(int userId) throws Exception {
        List<Budget> budgets = budgetDAO.findByUserId(userId);
        for (Budget b : budgets) {
            Map<String, Double> spending = transactionDAO.getCategorySpending(userId, b.getMonth());
            b.setSpent(spending.getOrDefault(b.getCategory(), 0.0));
        }
        return budgets;
    }

    // ── Summary ───────────────────────────────────────────────────────────────

    /**
     * Returns total income, total expense, net balance, and category breakdown for a month.
     */
    public Map<String, Object> getMonthlySummary(int userId, String month) throws Exception {
        List<Transaction> all = transactionDAO.findByUserIdAndDateRange(
            userId, month + "-01", month + "-31");

        // Use Iterator pattern to compute totals
        TransactionIterator incomeIt  = new TransactionIterator(all, "INCOME");
        TransactionIterator expenseIt = new TransactionIterator(all, "EXPENSE");

        double totalIncome = 0;
        while (incomeIt.hasNext())  totalIncome  += incomeIt.next().getAmount();

        double totalExpense = 0;
        while (expenseIt.hasNext()) totalExpense += expenseIt.next().getAmount();

        Map<String, Double> categoryBreakdown = transactionDAO.getCategorySpending(userId, month);

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("month",             month);
        summary.put("totalIncome",       totalIncome);
        summary.put("totalExpense",      totalExpense);
        summary.put("netBalance",        totalIncome - totalExpense);
        summary.put("categoryBreakdown", categoryBreakdown);
        return summary;
    }

    // ── Reports ───────────────────────────────────────────────────────────────

    /**
     * Builds a Report using the Builder pattern for the given date range.
     */
    public Report buildReport(int userId, String from, String to) throws Exception {
        User user = userDAO.findById(userId);
        List<Transaction> txns = transactionDAO.findByUserIdAndDateRange(userId, from, to);

        // Use Iterator to compute totals
        TransactionIterator incomeIt  = new TransactionIterator(txns, "INCOME");
        TransactionIterator expenseIt = new TransactionIterator(txns, "EXPENSE");

        double totalIncome = 0;
        while (incomeIt.hasNext())  totalIncome  += incomeIt.next().getAmount();

        double totalExpense = 0;
        while (expenseIt.hasNext()) totalExpense += expenseIt.next().getAmount();

        // Build category breakdown from all transactions in range
        Map<String, Double> breakdown = new LinkedHashMap<>();
        for (Transaction t : txns) {
            if ("EXPENSE".equals(t.getType())) {
                breakdown.merge(t.getCategory(), t.getAmount(), Double::sum);
            }
        }

        // Convert transactions to string rows for CSV export
        List<String[]> rows = new ArrayList<>();
        for (Transaction t : txns) {
            rows.add(new String[]{t.getDate(), t.getType(), t.getCategory(),
                                   String.valueOf(t.getAmount()), t.getDescription()});
        }

        // Builder constructs the final Report object step-by-step
        return new Report.Builder()
            .title("Finance Report: " + from + " to " + to)
            .username(user != null ? user.getUsername() : "Unknown")
            .dateRange(from + " to " + to)
            .totalIncome(totalIncome)
            .totalExpense(totalExpense)
            .netBalance(totalIncome - totalExpense)
            .categoryBreakdown(breakdown)
            .transactions(rows)
            .format("CSV")
            .generatedAt(LocalDate.now().toString())
            .build();
    }

    /** Converts a Report to CSV string. */
    public String reportToCsv(Report report) {
        StringBuilder sb = new StringBuilder();
        sb.append("Finance Report\n");
        sb.append("User: ").append(report.getUsername()).append("\n");
        sb.append("Period: ").append(report.getDateRange()).append("\n");
        sb.append("Generated: ").append(report.getGeneratedAt()).append("\n\n");
        sb.append("Summary\n");
        sb.append("Total Income,").append(report.getTotalIncome()).append("\n");
        sb.append("Total Expense,").append(report.getTotalExpense()).append("\n");
        sb.append("Net Balance,").append(report.getNetBalance()).append("\n\n");
        sb.append("Category Breakdown (Expenses)\n");
        sb.append("Category,Amount\n");
        report.getCategoryBreakdown().forEach((cat, amt) ->
            sb.append(cat).append(",").append(amt).append("\n"));
        sb.append("\nTransactions\n");
        sb.append("Date,Type,Category,Amount,Description\n");
        for (String[] row : report.getTransactions()) {
            sb.append(String.join(",", row)).append("\n");
        }
        return sb.toString();
    }

    // ── Recurring Transactions ─────────────────────────────────────────────────

    public RecurringTransaction addRecurring(RecurringTransaction rt) throws Exception {
        return recurringDAO.add(rt);
    }

    public void deleteRecurring(int id) throws Exception {
        recurringDAO.delete(id);
    }

    public List<RecurringTransaction> getRecurring(int userId) throws Exception {
        return recurringDAO.findByUserId(userId);
    }

    /**
     * Checks for recurring transactions that are due and auto-creates them as real transactions.
     * Advances the next_date after each processed entry.
     */
    private void processDueRecurring(int userId) throws Exception {
        List<RecurringTransaction> due = recurringDAO.findDue(userId);
        for (RecurringTransaction rt : due) {
            // Create a real transaction from the recurring template
            Transaction t = TransactionFactory.create(
                rt.getType(), userId, rt.getCategory(),
                rt.getAmount(), "[Auto] " + rt.getDescription(), rt.getNextDate());
            AddTransactionCommand cmd = new AddTransactionCommand(transactionDAO, t);
            cmd.execute();
            // Advance the schedule
            recurringDAO.advanceNextDate(rt.getId(), rt.getFrequency(), rt.getNextDate());
        }
    }
}
