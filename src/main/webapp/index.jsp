<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>TODO List Application</title>
    <!-- Bootstrap 5 CSS CDN -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <!-- Bootstrap Icons CDN -->
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.0/font/bootstrap-icons.css">
    <!-- Custom Styles -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/styles.css">
</head>
<body>
    <div class="container">
        <!-- Header -->
        <div class="todo-header">
            <h1><i class="bi bi-check2-circle"></i> My Tasks</h1>
            <p>Organize your work, boost your productivity</p>
        </div>

        <!-- Main Content -->
        <div class="todo-content">
            <!-- Alert Messages -->
            <div id="alertContainer"></div>

            <!-- Add Task Section -->
            <div class="add-task-section">
                <input 
                    type="text" 
                    id="taskInput" 
                    placeholder="Add a new task..." 
                    autocomplete="off"
                >
                <button id="addTaskBtn" onclick="addTask()">
                    <i class="bi bi-plus-lg"></i> Add
                </button>
            </div>

            <!-- Tasks List -->
            <div id="tasksList" class="tasks-list">
                <!-- Tasks will be populated here by JavaScript -->
            </div>

            <!-- Empty State -->
            <div id="emptyState" class="empty-state" style="display: none;">
                <div class="empty-state-icon">
                    <i class="bi bi-inbox"></i>
                </div>
                <p>No tasks yet. Add one to get started!</p>
            </div>
        </div>
    </div>

    <!-- Bootstrap 5 JS CDN -->
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>

    <script>
        // API Base URL
        const API_BASE_URL = '${pageContext.request.contextPath}/api/tasks';
        let tasks = [];
        let editingTaskId = null;

        // ==================== INITIALIZATION ====================
        document.addEventListener('DOMContentLoaded', function() {
            loadTasks();
            
            // Add task on Enter key
            document.getElementById('taskInput').addEventListener('keypress', function(e) {
                if (e.key === 'Enter') {
                    addTask();
                }
            });
        });

        // ==================== LOAD TASKS ====================
        function loadTasks() {
            fetch(API_BASE_URL + '/')
                .then(response => {
                    if (!response.ok) {
                        throw new Error('Failed to load tasks');
                    }
                    return response.json();
                })
                .then(data => {
                    tasks = data;
                    renderTasks();
                })
                .catch(error => {
                    console.error('Error loading tasks:', error);
                    showAlert('Error loading tasks', 'error');
                });
        }

        // ==================== RENDER TASKS ====================
        function renderTasks() {
            const tasksList = document.getElementById('tasksList');
            const emptyState = document.getElementById('emptyState');

            if (tasks.length === 0) {
                tasksList.innerHTML = '';
                emptyState.style.display = 'block';
                return;
            }

            emptyState.style.display = 'none';
            tasksList.innerHTML = tasks.map(task => createTaskHTML(task)).join('');
            
            // Attach event listeners to checkboxes
            tasks.forEach(task => {
                const checkbox = document.getElementById('checkbox-' + task.id);
                if (checkbox) {
                    checkbox.addEventListener('change', function() {
                        updateTaskCompletion(task.id, this.checked);
                    });
                }
            });
        }

        // ==================== CREATE TASK HTML ====================
        function createTaskHTML(task) {
            const completedClass = task.completed ? 'completed' : '';
            const checkedAttr = task.completed ? 'checked' : '';
            let html = '<div class="task-item ' + completedClass + '" data-id="' + task.id + '">' +
                '<input type="checkbox" class="task-checkbox" id="checkbox-' + task.id + '" ' + checkedAttr + '>' +
                '<span class="task-title" id="title-' + task.id + '">' + escapeHtml(task.title) + '</span>' +
                '<div class="task-buttons">' +
                '<button class="edit-btn" onclick="startEditTask(' + task.id + ')"><i class="bi bi-pencil"></i></button>' +
                '<button class="delete-btn" onclick="deleteTask(' + task.id + ')"><i class="bi bi-trash"></i></button>' +
                '</div></div>';
            return html;
        }

        // ==================== ADD TASK ====================
        function addTask() {
            const input = document.getElementById('taskInput');
            const title = input.value.trim();

            if (!title) {
                showAlert('Please enter a task name', 'error');
                return;
            }

            const newTask = {
                title: title,
                completed: false
            };

            fetch(API_BASE_URL + '/', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(newTask)
            })
            .then(response => {
                if (!response.ok) {
                    throw new Error('Failed to add task');
                }
                return response.json();
            })
            .then(data => {
                input.value = '';
                tasks.push(data);
                renderTasks();
                showAlert('Task added successfully', 'success');
            })
            .catch(error => {
                console.error('Error adding task:', error);
                showAlert('Error adding task', 'error');
            });
        }

        // ==================== UPDATE TASK COMPLETION ====================
        function updateTaskCompletion(id, completed) {
            const task = tasks.find(t => t.id === id);
            if (!task) return;

            fetch(API_BASE_URL + '/' + id, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({
                    title: task.title,
                    completed: completed
                })
            })
            .then(response => {
                if (!response.ok) {
                    throw new Error('Failed to update task');
                }
                // Update local state
                task.completed = completed;
                updateTaskItemDisplay(id);
            })
            .catch(error => {
                console.error('Error updating task:', error);
                showAlert('Error updating task', 'error');
                // Revert checkbox
                document.getElementById('checkbox-' + id).checked = !completed;
            });
        }

        // ==================== UPDATE TASK ITEM DISPLAY ====================
        function updateTaskItemDisplay(id) {
            const taskItem = document.querySelector(`.task-item[data-id="${id}"]`);
            if (!taskItem) return;

            const task = tasks.find(t => t.id === id);
            if (task.completed) {
                taskItem.classList.add('completed');
            } else {
                taskItem.classList.remove('completed');
            }
        }

        // ==================== START EDIT TASK ====================
        function startEditTask(id) {
            if (editingTaskId !== null) {
                // Cancel any existing edit
                cancelEditTask();
            }

            const task = tasks.find(t => t.id === id);
            if (!task) return;

            editingTaskId = id;
            const titleElement = document.getElementById('title-' + id);
            const currentTitle = task.title;

            // Replace title with input
            const input = document.createElement('input');
            input.type = 'text';
            input.className = 'task-edit-input';
            input.value = currentTitle;
            input.id = 'edit-input-' + id;

            titleElement.replaceWith(input);

            // Focus and select text
            input.focus();
            input.select();

            // Handle blur
            input.addEventListener('blur', function() {
                saveEditTask(id);
            });

            // Handle Enter key
            input.addEventListener('keypress', function(e) {
                if (e.key === 'Enter') {
                    saveEditTask(id);
                }
            });

            // Handle Escape key
            input.addEventListener('keydown', function(e) {
                if (e.key === 'Escape') {
                    cancelEditTask();
                }
            });
        }

        // ==================== SAVE EDIT TASK ====================
        function saveEditTask(id) {
            const input = document.getElementById('edit-input-' + id);
            if (!input) return;

            const newTitle = input.value.trim();
            const task = tasks.find(t => t.id === id);

            if (!newTitle) {
                showAlert('Task name cannot be empty', 'error');
                input.value = task.title;
                input.focus();
                return;
            }

            if (newTitle === task.title) {
                cancelEditTask();
                return;
            }

            fetch(API_BASE_URL + '/' + id, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({
                    title: newTitle,
                    completed: task.completed
                })
            })
            .then(response => {
                if (!response.ok) {
                    throw new Error('Failed to update task');
                }
                return response.json();
            })
            .then(data => {
                // Update local state
                task.title = newTitle;
                editingTaskId = null;
                renderTasks();
                showAlert('Task updated successfully', 'success');
            })
            .catch(error => {
                console.error('Error updating task:', error);
                showAlert('Error updating task', 'error');
                input.focus();
            });
        }

        // ==================== CANCEL EDIT TASK ====================
        function cancelEditTask() {
            if (editingTaskId === null) return;

            const input = document.getElementById('edit-input-' + editingTaskId);
            if (input) {
                const task = tasks.find(t => t.id === editingTaskId);
                const titleSpan = document.createElement('span');
                titleSpan.className = 'task-title';
                titleSpan.id = 'title-' + editingTaskId;
                titleSpan.textContent = task.title;
                input.replaceWith(titleSpan);
            }

            editingTaskId = null;
        }

        // ==================== DELETE TASK ====================
        function deleteTask(id) {
            if (!confirm('Are you sure you want to delete this task?')) {
                return;
            }

            fetch(API_BASE_URL + '/' + id, {
                method: 'DELETE'
            })
            .then(response => {
                if (!response.ok) {
                    throw new Error('Failed to delete task');
                }
                tasks = tasks.filter(t => t.id !== id);
                renderTasks();
                showAlert('Task deleted successfully', 'success');
            })
            .catch(error => {
                console.error('Error deleting task:', error);
                showAlert('Error deleting task', 'error');
            });
        }

        // ==================== SHOW ALERT ====================
        function showAlert(message, type) {
            const alertContainer = document.getElementById('alertContainer');
            const alertClass = type === 'error' ? 'alert-error' : 'alert-success';
            const alertElement = document.createElement('div');
            alertElement.className = 'alert ' + alertClass + ' show';
            alertElement.innerText = message;

            alertContainer.innerHTML = '';
            alertContainer.appendChild(alertElement);

            // Auto-hide after 3 seconds
            setTimeout(() => {
                alertElement.remove();
            }, 3000);
        }

        // ==================== UTILITY: ESCAPE HTML ====================
        function escapeHtml(text) {
            if (!text) return '';
            const map = {
                '&': '&amp;',
                '<': '&lt;',
                '>': '&gt;',
                '"': '&quot;',
                "'": '&#039;'
            };
            return text.replace(/[&<>"']/g, m => map[m]);
        }
    </script>
</body>
</html>
