package http;

import com.google.gson.Gson;
import manager.InMemoryTaskManager;
import manager.TaskManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import tasks.Epic;
import tasks.Status;
import tasks.Subtask;
import tasks.Task;

import java.io.IOException;
import java.net.http.HttpClient;
import java.time.Duration;
import java.time.LocalDateTime;

public abstract class BaseHttpEndpointTest {

    protected TaskManager manager;
    protected HttpTaskServer taskServer;
    protected Gson gson;
    protected HttpClient client;

    // Общие тестовые данные
    protected Task testTask1;
    protected Task testTask2;
    protected Epic testEpic;
    protected Subtask testSubtask;
    protected LocalDateTime testStartTime;

    @BeforeEach
    public void setUp() throws IOException {
        manager = new InMemoryTaskManager();
        taskServer = new HttpTaskServer(manager);
        gson = HttpTaskServer.getGson();
        client = HttpClient.newHttpClient();
        taskServer.start();

        setupTestData();
    }

    @AfterEach
    public void shutDown() {
        taskServer.stop();
    }

    protected void setupTestData() {
        testStartTime = LocalDateTime.now();

        testTask1 = new Task("Test Task 1", "Description 1");
        testTask2 = new Task("Test Task 2", "Description 2", Duration.ofHours(1), testStartTime);

        testEpic = new Epic("Test Epic", "Epic description");

        manager.addEpic(testEpic);
        testSubtask = new Subtask("Test Subtask", "Subtask description", Status.NEW, testEpic.getId());
    }

    protected void addTestTasksToManager() {
        manager.addTask(testTask1);
        manager.addTask(testTask2);
        manager.addSubtask(testSubtask);
    }
}