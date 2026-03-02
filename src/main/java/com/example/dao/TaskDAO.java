// CHECKSTYLE:OFF
package com.example.dao;

import com.example.model.Task;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Data Access Object for {@link com.example.model.Task} entities.
 * <p>
 * Handles all JDBC interactions with an embedded SQLite database. The
 * database file is created automatically when the first DAO instance is
 * constructed. Methods return simple results (entity or boolean) and log
 * SQLExceptions instead of propagating them to callers.
 */
public class TaskDAO {
    private static final Logger LOGGER = Logger.getLogger(TaskDAO.class.getName());
    /** JDBC connection URL for SQLite. */
    private static final String DB_URL = "jdbc:sqlite:todo.db";
    /** SQL used on start‑up to ensure the tasks table exists.
     *  Broken into pieces for style rules.
     */
    private static final String CREATE_TABLE_SQL =
            "CREATE TABLE IF NOT EXISTS tasks ("
            + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
            + "title TEXT NOT NULL,"
            + "completed BOOLEAN NOT NULL DEFAULT 0)";
    /** index of the id parameter in prepared statements */
    private static final int PARAM_ID_INDEX = 3;

    /** flag used to ensure database initialization happens only once */
    private static volatile boolean initialized;

    /**
     * Constructs a new DAO instance and ensures the database is initialized.
     */
    public TaskDAO() {
        initializeDatabaseOnce();
    }

    private static void initializeDatabaseOnce() {
        if (!initialized) {
            synchronized (TaskDAO.class) {
                if (!initialized) {
                    try {
                        Class.forName("org.sqlite.JDBC");
                    } catch (ClassNotFoundException e) {
                        LOGGER.log(Level.SEVERE, "JDBC driver not found", e);
                    }
                    try (Connection conn = getConnection();
                         Statement stmt = conn.createStatement()) {
                        stmt.execute(CREATE_TABLE_SQL);
                        initialized = true;
                    } catch (SQLException e) {
                        LOGGER.log(Level.SEVERE, "Failed to initialize database", e);
                    }
                }
            }
        }
    }

    private static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    /**
     * Inserts the given task into the database. The task's {@code id}
     * property will be populated with the generated key on success.
     *
     * @param task non-null task with a title
     * @return same task instance with id set, or the original task if an error
     * occurred
     */
    public Task create(final Task task) {
        final String sql = "INSERT INTO tasks (title, completed) VALUES (?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, task.getTitle());
            pstmt.setBoolean(2, task.isCompleted());
            pstmt.executeUpdate();

            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    task.setId(rs.getInt(1));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error inserting task", e);
        }
        return task;
    }

    /**
     * Retrieves a task by its primary key.
     *
     * @param id identifier of the task
     * @return matching {@link Task} or {@code null} if not found or on error
     */
    public Task read(final int id) {
        final String sql = "SELECT id, title, completed FROM tasks WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Task(rs.getInt("id"), rs.getString("title"), rs.getBoolean("completed"));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error reading task with id " + id, e);
        }
        return null;
    }

    /**
     * Returns all tasks stored in the database. If an error occurs an empty
     * list is returned.
     *
     * @return list of tasks (never {@code null})
     */
    public List<Task> readAll() {
        final List<Task> tasks = new ArrayList<>();
        final String sql = "SELECT id, title, completed FROM tasks";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                tasks.add(new Task(rs.getInt("id"), rs.getString("title"), rs.getBoolean("completed")));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error reading all tasks", e);
        }
        return tasks;
    }

    /**
     * Updates an existing task. The task must have a valid {@code id}.
     *
     * @param task task containing new values
     * @return {@code true} if a row was modified, {@code false} otherwise or on error
     */
    public boolean update(final Task task) {
        final String sql = "UPDATE tasks SET title = ?, completed = ? WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, task.getTitle());
            pstmt.setBoolean(2, task.isCompleted());
            pstmt.setInt(PARAM_ID_INDEX, task.getId());
            final int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating task with id " + task.getId(), e);
        }
        return false;
    }

    /**
     * Removes the task with the given id.
     *
     * @param id identifier of the task to delete
     * @return {@code true} if a row was deleted, {@code false} if none or on error
     */
    public boolean delete(final int id) {
        final String sql = "DELETE FROM tasks WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            final int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error deleting task with id " + id, e);
        }
        return false;
    }
}
