// CHECKSTYLE:OFF
package com.example.model;

/**
 * Simple POJO representing a to‑do task.
 */
public final class Task {
    /** database auto-generated primary key. */
    private int id;
    /** human-readable title of the task. */
    private String title;
    /** true when the task has been completed. */
    private boolean completed;

    public Task() {
    }

    /**
     * Full constructor used when all fields are known.
     *
     * @param taskId primary key value.
     * @param taskTitle title text.
     * @param taskCompleted completion flag.
     */
    public Task(final int taskId,
                final String taskTitle,
                final boolean taskCompleted) {
        this.id = taskId;
        this.title = taskTitle;
        this.completed = taskCompleted;
    }

    /**
     * Convenience constructor for new tasks (no id yet).
     */
    public Task(final String taskTitle,
                final boolean taskCompleted) {
        this.title = taskTitle;
        this.completed = taskCompleted;
    }

    /**
     * Returns the task's primary key.
     *
     * @return numeric identifier of this task
     */
    public int getId() {
        return id;
    }

    /**
     * Sets the task's primary key.
     *
     * @param taskId new id value
     */
    public void setId(final int taskId) {
        this.id = taskId;
    }

    /**
     * Returns the task title.
     *
     * @return title string
     */
    public String getTitle() {
        return title;
    }

    /**
     * Updates the task title.
     *
     * @param taskTitle new title text
     */
    public void setTitle(final String taskTitle) {
        this.title = taskTitle;
    }

    /**
     * Indicates whether the task is complete.
     *
     * @return {@code true} when completed
     */
    public boolean isCompleted() {
        return completed;
    }

    /**
     * Marks the task as complete/incomplete.
     *
     * @param taskCompleted new completion flag
     */
    public void setCompleted(final boolean taskCompleted) {
        this.completed = taskCompleted;
    }

    @Override
    public String toString() {
        return "Task{"
                + "id=" + id
                + ", title='" + title + '\''
                + ", completed=" + completed
                + '}';
    }

    /**
     * Standard equality comparing all fields.
     *
     * @param o other object to compare
     * @return {@code true} if objects are equal
     */
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Task task = (Task) o;
        if (id != task.id) {
            return false;
        }
        if (completed != task.completed) {
            return false;
        }
        return title != null ? title.equals(task.title) : task.title == null;
    }

    @Override
    public int hashCode() {
        int result = id;
        result = HASH * result + (title != null ? title.hashCode() : 0);
        result = HASH * result + (completed ? 1 : 0);
        return result;
    }

    /** multiplier used in {@link #hashCode()} to reduce collisions */
    private static final int HASH = 31;
}
