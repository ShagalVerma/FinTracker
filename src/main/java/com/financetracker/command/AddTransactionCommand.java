package com.financetracker.command;

import com.financetracker.dao.TransactionDAO;
import com.financetracker.model.Transaction;

/**
 * DESIGN PATTERN: Command (concrete command)
 *
 * Encapsulates the "add a transaction" operation.
 * On execute() it persists the transaction to the DB.
 * On undo()    it deletes the just-added transaction.
 */
public class AddTransactionCommand implements Command {

    private final TransactionDAO dao;
    private final Transaction transaction;

    public AddTransactionCommand(TransactionDAO dao, Transaction transaction) {
        this.dao = dao;
        this.transaction = transaction;
    }

    @Override
    public void execute() throws Exception {
        int id = dao.add(transaction);
        transaction.setId(id); // store generated ID for potential undo
    }

    @Override
    public void undo() throws Exception {
        if (transaction.getId() > 0) {
            dao.delete(transaction.getId());
        }
    }

    public Transaction getTransaction() {
        return transaction;
    }
}
