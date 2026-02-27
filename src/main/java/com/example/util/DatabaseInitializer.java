// CHECKSTYLE:OFF
package com.example.util;

import com.example.dao.TaskDAO;

import java.util.logging.Logger;

/**
 * Simple helper to create/initialize the SQLite database outside of a servlet container.
 * <p>
 * Can be executed via <code>mvn exec:java</code> or directly from an IDE. It simply
 * instantiates the DAO which triggers the database initialization logic.
 */
public final class DatabaseInitializer {
    private static final Logger LOGGER = Logger.getLogger(DatabaseInitializer.class.getName());

    // prevent instantiation
    private DatabaseInitializer() {
        // utility class
    }

    /**
     * Entry point when run by Maven or IDE.
     * Initializes the embedded SQLite database file.
     */
    public static void main(final String[] args) {
        LOGGER.info("Initializing SQLite database (todo.db) …");
        // construction triggers initialization in TaskDAO
        new TaskDAO();
        LOGGER.info("Initialization complete. Check todo.db in project root.");
    }
}
