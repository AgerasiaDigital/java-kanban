package http.handler;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import manager.NotFoundException;
import manager.TaskManager;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public abstract class BaseHttpHandler implements HttpHandler {
    protected final TaskManager taskManager;
    protected final Gson gson;

    public BaseHttpHandler(TaskManager taskManager, Gson gson) {
        this.taskManager = taskManager;
        this.gson = gson;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            handleRequest(exchange);
        } catch (NotFoundException e) {
            sendResponse(exchange, 404, "");
        } catch (IllegalArgumentException e) {
            sendResponse(exchange, 406, "");
        } catch (Exception e) {
            sendResponse(exchange, 500, "");
        }
    }

    protected abstract void handleRequest(HttpExchange exchange) throws IOException;

    protected void sendResponse(HttpExchange exchange, int statusCode, String text) throws IOException {
        byte[] resp = text.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json;charset=utf-8");
        exchange.sendResponseHeaders(statusCode, resp.length);
        exchange.getResponseBody().write(resp);
        exchange.close();
    }

    protected void sendText(HttpExchange exchange, String text) throws IOException {
        sendResponse(exchange, 200, text);
    }

    protected void sendNotFound(HttpExchange exchange) throws IOException {
        sendResponse(exchange, 404, "");
    }

    protected void sendHasInteractions(HttpExchange exchange) throws IOException {
        sendResponse(exchange, 406, "");
    }

    protected void sendCreated(HttpExchange exchange) throws IOException {
        sendResponse(exchange, 201, "");
    }

    protected void sendInternalServerError(HttpExchange exchange) throws IOException {
        sendResponse(exchange, 500, "");
    }
}