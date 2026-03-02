// CHECKSTYLE:OFF
package com.example.servlet;

import com.example.dao.TaskDAO;
import com.example.model.Task;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * REST‑style servlet exposing CRUD operations for {@link Task} objects.
 * Requests and responses use JSON; the servlet relies on Gson for
 * serialization/deserialization.
 */
@WebServlet("/api/tasks/*")
public final class TasksServlet extends HttpServlet {
    /** Serialization UID for servlet (never changed). */
    private static final long serialVersionUID = 1L;
    /** Logger for request handling errors. */
    private static final Logger LOGGER = Logger.getLogger(TasksServlet.class.getName());
    /** MIME type returned by all endpoints. */
    private static final String JSON_CONTENT_TYPE = "application/json";
    /** Character encoding used throughout. */
    private static final String UTF8 = "UTF-8";

    /** DAO used for task persistence. */
    private final TaskDAO taskDAO = new TaskDAO();
    /** Gson instance shared across requests. */
    private final Gson gson = new Gson();

/**
     * Handles GET requests.  If an ID is provided in the path, a single task
     * is returned; otherwise the full list is returned.
     */
    @Override
    protected void doGet(final HttpServletRequest request,
                         final HttpServletResponse response)
            throws IOException {
        configureResponse(response);
        try (PrintWriter out = response.getWriter()) {
            Optional<Integer> optId = parseId(request.getPathInfo());
            if (optId.isPresent()) {
                int id = optId.get();
                Task task = taskDAO.read(id);
                if (task != null) {
                    writeJson(out, task);
                } else {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND, "Task not found");
                }
            } else {
                List<Task> tasks = taskDAO.readAll();
                writeJson(out, tasks);
            }
        }
    }

/**
     * Creates a new task from the JSON body and returns it with its generated
     * ID.
     */
    @Override
    protected void doPost(final HttpServletRequest request,
                          final HttpServletResponse response)
            throws IOException {
        configureResponse(response);
        try (PrintWriter out = response.getWriter()) {
            Task task = parseTaskFromRequest(request);
            if (task == null || task.getTitle() == null || task.getTitle().trim().isEmpty()) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Title is required");
                return;
            }
            task = taskDAO.create(task);
            response.setStatus(HttpServletResponse.SC_CREATED);
            writeJson(out, task);
        } catch (JsonSyntaxException e) {
            LOGGER.log(Level.WARNING, "Invalid JSON in POST", e);
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Malformed JSON");
        }
    }

/**
     * Updates an existing task.  The task ID is taken from the path.
     */
    @Override
    protected void doPut(final HttpServletRequest request,
                         final HttpServletResponse response)
            throws IOException {
        configureResponse(response);
        try (PrintWriter out = response.getWriter()) {
            Optional<Integer> optId = parseId(request.getPathInfo());
            if (!optId.isPresent()) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Task ID required");
                return;
            }
            int id = optId.get();
            Task task = parseTaskFromRequest(request);
            if (task == null || task.getTitle() == null || task.getTitle().trim().isEmpty()) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Title is required");
                return;
            }
            task.setId(id);
            if (taskDAO.update(task)) {
                writeJson(out, task);
            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Task not found");
            }
        } catch (JsonSyntaxException e) {
            LOGGER.log(Level.WARNING, "Invalid JSON in PUT", e);
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Malformed JSON");
        }
    }

/**
     * Deletes the task identified in the path.
     */
    @Override
    protected void doDelete(final HttpServletRequest request,
                            final HttpServletResponse response)
            throws IOException {
        configureResponse(response);
        try (PrintWriter out = response.getWriter()) {
            Optional<Integer> optId = parseId(request.getPathInfo());
            if (!optId.isPresent()) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Task ID required");
                return;
            }
            int id = optId.get();
            if (taskDAO.delete(id)) {
                writeJson(out, new Message("Task deleted"));
            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Task not found");
            }
        }
    }

    private void configureResponse(final HttpServletResponse response) {
        response.setContentType(JSON_CONTENT_TYPE);
        response.setCharacterEncoding(UTF8);
    }

    /**
     * Extracts an integer ID from the path info, if present.
     *
     * @param pathInfo raw value of {@code request.getPathInfo()}
     * @return empty if no ID was supplied
     * @throws NumberFormatException if the segment is not a valid integer
     */
    private Optional<Integer> parseId(final String pathInfo) {
        if (pathInfo == null || pathInfo.equals("/")) {
            return Optional.empty();
        }
        String[] parts = pathInfo.split("/");
        if (parts.length > 1 && !parts[1].isEmpty()) {
            return Optional.of(Integer.parseInt(parts[1]));
        }
        return Optional.empty();
    }

    /**
     * Reads the request body and converts JSON into a {@link Task}.
     *
     * @param request HTTP request whose body contains JSON
     * @return deserialized task (may be {@code null})
     * @throws IOException on read failure
     */
    private Task parseTaskFromRequest(final HttpServletRequest request)
            throws IOException {
        try (BufferedReader reader = request.getReader()) {
            return gson.fromJson(reader, Task.class);
        }
    }

    private void writeJson(final PrintWriter out, final Object obj) {
        out.print(gson.toJson(obj));
    }

    // simple payload class for delete response
    private static class Message {
        /** textual message returned to client */
        private final String text;

        Message(final String message) {
            this.text = message;
        }
    }
}

