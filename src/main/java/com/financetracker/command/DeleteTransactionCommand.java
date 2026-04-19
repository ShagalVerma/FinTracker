package com.financetracker.command;

import com.financetracker.dao.TransactionDAO;
import com.financetracker.model.Transaction;

/**
 * DESIGN PATTERN: Command (concrete command)
 *
 * Encapsulates the "delete a transaction" operation.
 * Before execute() it fetches the transaction as a backup so that undo() can restore it.
 */
public class DeleteTransactionCommand implements Command {

    private final TransactionDAO dao;
    private final int transactionId;
    private Transaction backup;

    public DeleteTransactionCommand(TransactionDAO dao, int transactionId) throws Exception {
        this.dao = dao;
        this.transactionId = transactionId;
        // Fetch backup before deletion so undo can restore it
        this.backup = dao.findById(transactionId);
    }

    @Override
    public void execute() throws Exception {
        dao.delete(transactionId);
    }

    @Override
    public void undo() throws Exception {
        if (backup != null) {
            // Re-insert the deleted transaction
            backup.setId(0); // reset ID so DB auto-assigns
            dao.add(backup);
        }
    }
}
