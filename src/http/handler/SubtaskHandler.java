package http.handler;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import manager.TaskManager;
import tasks.Subtask;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class SubtaskHandler extends BaseHttpHandler {

    public SubtaskHandler(TaskManager taskManager, Gson gson) {
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
        if (path.equals("/subtasks")) {
            List<Subtask> subtasks = taskManager.getAllSubtasks();
            String subtasksJson = gson.toJson(subtasks);
            sendText(exchange, subtasksJson);
        } else if (path.startsWith("/subtasks/")) {
            String[] pathParts = path.split("/");
            if (pathParts.length == 3) {
                try {
                    int subtaskId = Integer.parseInt(pathParts[2]);
                    Subtask subtask = taskManager.getSubtaskById(subtaskId);
                    String subtaskJson = gson.toJson(subtask);
                    sendText(exchange, subtaskJson);
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

        Subtask subtask = gson.fromJson(body, Subtask.class);

        if (subtask.getId() == 0) {
            // Создание новой подзадачи - логика проверки эпика в TaskManager
            taskManager.addSubtask(subtask);
        } else {
            // Обновление существующей подзадачи
            taskManager.updateSubtask(subtask);
        }
        sendCreated(exchange);
    }

    private void handleDelete(HttpExchange exchange, String path) throws IOException {
        if (path.startsWith("/subtasks/")) {
            String[] pathParts = path.split("/");
            if (pathParts.length == 3) {
                try {
                    int subtaskId = Integer.parseInt(pathParts[2]);
                    // Логика проверки существования в TaskManager
                    taskManager.deleteSubtaskById(subtaskId);
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