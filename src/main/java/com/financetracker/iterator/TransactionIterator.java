package com.financetracker.iterator;

import com.financetracker.model.Transaction;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * DESIGN PATTERN: Iterator
 *
 * Provides a way to sequentially access elements of a transaction collection
 * without exposing its underlying representation.
 *
 * This implementation supports optional type-filtering ("INCOME" or "EXPENSE")
 * so callers can iterate only over the subset they care about — which is used
 * by ReportService to separately sum income and expenses.
 *
 * Usage:
 *   TransactionIterator it = new TransactionIterator(transactions, "EXPENSE");
 *   while (it.hasNext()) {
 *       Transaction t = it.next();
 *       total += t.getAmount();
 *   }
 */
public class TransactionIterator implements Iterator<Transaction> {

    private final List<Transaction> transactions;
    private final String typeFilter;    // null → no filter, otherwise "INCOME" or "EXPENSE"
    private int cursor = 0;
    private Transaction peeked = null;
    private boolean hasPeeked = false;

    public TransactionIterator(List<Transaction> transactions, String typeFilter) {
        this.transactions = transactions;
        this.typeFilter   = (typeFilter == null) ? null : typeFilter.toUpperCase();
    }

    /** Constructor with no filter — iterates all transactions. */
    public TransactionIterator(List<Transaction> transactions) {
        this(transactions, null);
    }

    @Override
    public boolean hasNext() {
        if (hasPeeked) return true;
        while (cursor < transactions.size()) {
            Transaction t = transactions.get(cursor++);
            if (typeFilter == null || typeFilter.equals(t.getType())) {
                peeked    = t;
                hasPeeked = true;
                return true;
            }
        }
        return false;
    }

    @Override
    public Transaction next() {
        if (!hasNext()) throw new NoSuchElementException("No more transactions.");
        hasPeeked = false;
        return peeked;
    }
}
