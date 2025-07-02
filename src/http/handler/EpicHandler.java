package http.handler;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import manager.TaskManager;
import tasks.Epic;
import tasks.Subtask;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class EpicHandler extends BaseHttpHandler {

    public EpicHandler(TaskManager taskManager, Gson gson) {
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
        if (path.equals("/epics")) {
            List<Epic> epics = taskManager.getAllEpics();
            String epicsJson = gson.toJson(epics);
            sendText(exchange, epicsJson);
        } else if (path.startsWith("/epics/")) {
            String[] pathParts = path.split("/");
            if (pathParts.length == 3) {
                try {
                    int epicId = Integer.parseInt(pathParts[2]);
                    Epic epic = taskManager.getEpicById(epicId);
                    String epicJson = gson.toJson(epic);
                    sendText(exchange, epicJson);
                } catch (NumberFormatException e) {
                    sendNotFound(exchange);
                }
            } else if (pathParts.length == 4 && pathParts[3].equals("subtasks")) {
                try {
                    int epicId = Integer.parseInt(pathParts[2]);
                    // Логика проверки существования эпика теперь в TaskManager
                    List<Subtask> epicSubtasks = taskManager.getSubtasksByEpicId(epicId);
                    String subtasksJson = gson.toJson(epicSubtasks);
                    sendText(exchange, subtasksJson);
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

        Epic epic = gson.fromJson(body, Epic.class);
        taskManager.addEpic(epic);
        sendCreated(exchange);
    }

    private void handleDelete(HttpExchange exchange, String path) throws IOException {
        if (path.startsWith("/epics/")) {
            String[] pathParts = path.split("/");
            if (pathParts.length == 3) {
                try {
                    int epicId = Integer.parseInt(pathParts[2]);
                    taskManager.deleteEpicById(epicId);
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