package com.financetracker.command;

/**
 * DESIGN PATTERN: Command
 *
 * Encapsulates a request (transaction operation) as an object.
 * This allows:
 *   - Parameterising methods with different operations
 *   - Queueing or logging operations
 *   - Supporting undo/redo functionality
 *
 * Concrete implementations: AddTransactionCommand, DeleteTransactionCommand
 */
public interface Command {
    /** Execute the operation. */
    void execute() throws Exception;

    /** Reverse the operation (undo). */
    void undo() throws Exception;
}
