package http.handler;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import manager.TaskManager;
import tasks.Task;

import java.io.IOException;
import java.util.List;

public class PrioritizedHandler extends BaseHttpHandler {

    public PrioritizedHandler(TaskManager taskManager, Gson gson) {
        super(taskManager, gson);
    }

    @Override
    protected void handleRequest(HttpExchange exchange) throws IOException {
        String requestMethod = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        if (requestMethod.equals("GET") && path.equals("/prioritized")) {
            List<Task> prioritizedTasks = taskManager.getPrioritizedTasks();
            String prioritizedJson = gson.toJson(prioritizedTasks);
            sendText(exchange, prioritizedJson);
        } else {
            sendResponse(exchange, 405, "");
        }
    }
}