package http;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpServer;
import http.adapters.DurationAdapter;
import http.adapters.LocalDateTimeAdapter;
import http.handler.*;
import manager.Managers;
import manager.TaskManager;
import tasks.Epic;
import tasks.Subtask;
import tasks.Task;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.time.LocalDateTime;

public class HttpTaskServer {
    private static final int PORT = 8080;
    private final TaskManager taskManager;
    private HttpServer httpServer;
    private final Gson gson;

    public HttpTaskServer(TaskManager taskManager) throws IOException {
        this.taskManager = taskManager;
        this.gson = createGson();
        this.httpServer = HttpServer.create(new InetSocketAddress(PORT), 0);
        setupHandlers();
    }

    public HttpTaskServer() throws IOException {
        this(Managers.getDefault());
    }

    private void setupHandlers() {
        httpServer.createContext("/tasks", new TaskHandler(taskManager, gson));
        httpServer.createContext("/subtasks", new SubtaskHandler(taskManager, gson));
        httpServer.createContext("/epics", new EpicHandler(taskManager, gson));
        httpServer.createContext("/history", new HistoryHandler(taskManager, gson));
        httpServer.createContext("/prioritized", new PrioritizedHandler(taskManager, gson));
    }

    public void start() {
        httpServer.start();
        System.out.println("HTTP-сервер запущен на порту " + PORT);
    }

    public void stop() {
        if (httpServer != null) {
            httpServer.stop(1); // Ждем 1 секунду для завершения текущих запросов
            System.out.println("HTTP-сервер остановлен");
        }
    }

    public static Gson getGson() {
        return createGson();
    }

    private static Gson createGson() {
        return new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .registerTypeAdapter(Duration.class, new DurationAdapter())
                .create();
    }

    public static void main(String[] args) {
        try {
            HttpTaskServer server = new HttpTaskServer();

            TaskManager manager = server.taskManager;

            Task task1 = new Task("Задача 1", "Описание задачи 1");
            Task task2 = new Task("Задача 2", "Описание задачи 2");
            manager.addTask(task1);
            manager.addTask(task2);

            Epic epic1 = new Epic("Эпик 1", "Описание эпика 1");
            manager.addEpic(epic1);

            Subtask subtask1 = new Subtask("Подзадача 1", "Описание подзадачи 1",
                    tasks.Status.NEW, epic1.getId());
            manager.addSubtask(subtask1);

            server.start();

            System.out.println("Сервер готов к работе!");
            System.out.println("Доступные эндпоинты:");
            System.out.println("GET http://localhost:8080/tasks");
            System.out.println("GET http://localhost:8080/epics");
            System.out.println("GET http://localhost:8080/subtasks");
            System.out.println("GET http://localhost:8080/history");
            System.out.println("GET http://localhost:8080/prioritized");
            System.out.println("\nДля остановки сервера нажмите Enter");

            System.in.read();

            System.out.println("Останавливаем сервер...");
            server.stop();
            System.out.println("Сервер остановлен.");

        } catch (IOException e) {
            System.err.println("Ошибка запуска сервера: " + e.getMessage());
        }
    }
}