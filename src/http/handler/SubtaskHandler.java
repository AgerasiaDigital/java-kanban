package http.handler;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import manager.NotFoundException;
import manager.TaskManager;
import tasks.Subtask;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class SubtaskHandler extends BaseHttpHandler implements HttpHandler {
    private final TaskManager taskManager;
    private final Gson gson;

    public SubtaskHandler(TaskManager taskManager, Gson gson) {
        this.taskManager = taskManager;
        this.gson = gson;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String requestMethod = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        try {
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
                    exchange.sendResponseHeaders(405, 0);
                    exchange.close();
            }
        } catch (NotFoundException e) {
            sendNotFound(exchange);
        } catch (IllegalArgumentException e) {
            sendHasInteractions(exchange);
        } catch (Exception e) {
            sendInternalServerError(exchange);
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
                    if (subtask == null) {
                        throw new NotFoundException("Subtask not found");
                    }
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
            taskManager.addSubtask(subtask);
            sendCreated(exchange);
        } else {
            taskManager.updateSubtask(subtask);
            sendCreated(exchange);
        }
    }

    private void handleDelete(HttpExchange exchange, String path) throws IOException {
        if (path.startsWith("/subtasks/")) {
            String[] pathParts = path.split("/");
            if (pathParts.length == 3) {
                try {
                    int subtaskId = Integer.parseInt(pathParts[2]);
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