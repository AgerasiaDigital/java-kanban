package http.handler;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import manager.TaskManager;
import tasks.Task;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class TaskHandler extends BaseHttpHandler {

    public TaskHandler(TaskManager taskManager, Gson gson) {
        super(taskManager, gson);
    }

    @Override
    protected void handleRequest(HttpExchange exchange) throws IOException {
        String requestMethod = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        switch (requestMethod) {
            case "GET":
                handleGet(exchange, path);
                break;
            case "POST":
                handlePost(exchange);
                break;
            case "DELETE":
                handleDelete(exchange, path);
                break;
            default:
                sendResponse(exchange, 405, "");
        }
    }

    private void handleGet(HttpExchange exchange, String path) throws IOException {
        if (path.equals("/tasks")) {
            List<Task> tasks = taskManager.getAllTasks();
            String tasksJson = gson.toJson(tasks);
            sendText(exchange, tasksJson);
        } else if (path.startsWith("/tasks/")) {
            String[] pathParts = path.split("/");
            if (pathParts.length == 3) {
                try {
                    int taskId = Integer.parseInt(pathParts[2]);
                    Task task = taskManager.getTaskById(taskId);
                    String taskJson = gson.toJson(task);
                    sendText(exchange, taskJson);
                } catch (NumberFormatException e) {
                    sendNotFound(exchange);
                }
            } else {
                sendNotFound(exchange);
            }
        } else {
            sendNotFound(exchange);
        }
    }

    private void handlePost(HttpExchange exchange) throws IOException {
        InputStream inputStream = exchange.getRequestBody();
        String body = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);

        Task task = gson.fromJson(body, Task.class);

        if (task.getId() == 0) {
            taskManager.addTask(task);
        } else {
            taskManager.updateTask(task);
        }
        sendCreated(exchange);
    }

    private void handleDelete(HttpExchange exchange, String path) throws IOException {
        if (path.startsWith("/tasks/")) {
            String[] pathParts = path.split("/");
            if (pathParts.length == 3) {
                try {
                    int taskId = Integer.parseInt(pathParts[2]);
                    taskManager.deleteTaskById(taskId);
                    sendText(exchange, "");
                } catch (NumberFormatException e) {
                    sendNotFound(exchange);
                }
            } else {
                sendNotFound(exchange);
            }
        } else {
            sendNotFound(exchange);
        }
    }
}