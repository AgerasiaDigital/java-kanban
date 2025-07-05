package http.handler;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import manager.TaskManager;
import tasks.Task;

import java.io.IOException;
import java.util.List;

public class HistoryHandler extends BaseHttpHandler {

    public HistoryHandler(TaskManager taskManager, Gson gson) {
        super(taskManager, gson);
    }

    @Override
    protected void handleRequest(HttpExchange exchange) throws IOException {
        String requestMethod = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        if (requestMethod.equals("GET") && path.equals("/history")) {
            List<Task> history = taskManager.getHistory();
            String historyJson = gson.toJson(history);
            sendText(exchange, historyJson);
        } else {
            sendResponse(exchange, 405, "");
        }
    }
}