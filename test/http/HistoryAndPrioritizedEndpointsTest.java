package http;

import com.google.gson.Gson;
import manager.InMemoryTaskManager;
import manager.TaskManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tasks.Epic;
import tasks.Status;
import tasks.Subtask;
import tasks.Task;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class HistoryAndPrioritizedEndpointsTest {

    private TaskManager manager;
    private HttpTaskServer taskServer;
    private Gson gson;
    private HttpClient client;

    @BeforeEach
    public void setUp() throws IOException {
        manager = new InMemoryTaskManager();
        taskServer = new HttpTaskServer(manager);
        gson = HttpTaskServer.getGson();
        client = HttpClient.newHttpClient();
        taskServer.start();
    }

    @AfterEach
    public void shutDown() {
        taskServer.stop();
    }

    @Test
    public void testGetEmptyHistory() throws IOException, InterruptedException {
        URI url = URI.create("http://localhost:8080/history");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        List<Task> history = gson.fromJson(response.body(),
                com.google.gson.reflect.TypeToken.getParameterized(List.class, Task.class).getType());
        assertTrue(history.isEmpty());
    }

    @Test
    public void testGetHistory() throws IOException, InterruptedException {
        Task task = new Task("Task", "Description");
        Epic epic = new Epic("Epic", "Description");
        manager.addTask(task);
        manager.addEpic(epic);

        Subtask subtask = new Subtask("Subtask", "Description", Status.NEW, epic.getId());
        manager.addSubtask(subtask);

        URI taskUrl = URI.create("http://localhost:8080/tasks/" + task.getId());
        HttpRequest taskRequest = HttpRequest.newBuilder().uri(taskUrl).GET().build();
        client.send(taskRequest, HttpResponse.BodyHandlers.ofString());

        URI epicUrl = URI.create("http://localhost:8080/epics/" + epic.getId());
        HttpRequest epicRequest = HttpRequest.newBuilder().uri(epicUrl).GET().build();
        client.send(epicRequest, HttpResponse.BodyHandlers.ofString());

        URI subtaskUrl = URI.create("http://localhost:8080/subtasks/" + subtask.getId());
        HttpRequest subtaskRequest = HttpRequest.newBuilder().uri(subtaskUrl).GET().build();
        client.send(subtaskRequest, HttpResponse.BodyHandlers.ofString());

        URI historyUrl = URI.create("http://localhost:8080/history");
        HttpRequest historyRequest = HttpRequest.newBuilder().uri(historyUrl).GET().build();

        HttpResponse<String> response = client.send(historyRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        List<Task> history = gson.fromJson(response.body(),
                com.google.gson.reflect.TypeToken.getParameterized(List.class, Task.class).getType());
        assertEquals(3, history.size());
    }

    @Test
    public void testHistoryMethodNotAllowed() throws IOException, InterruptedException {
        URI url = URI.create("http://localhost:8080/history");
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString("")).build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(405, response.statusCode()); // Method Not Allowed
    }

    @Test
    public void testGetEmptyPrioritizedTasks() throws IOException, InterruptedException {
        URI url = URI.create("http://localhost:8080/prioritized");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        List<Task> prioritizedTasks = gson.fromJson(response.body(),
                com.google.gson.reflect.TypeToken.getParameterized(List.class, Task.class).getType());
        assertTrue(prioritizedTasks.isEmpty());
    }

    @Test
    public void testGetPrioritizedTasks() throws IOException, InterruptedException {
        LocalDateTime now = LocalDateTime.now();

        Task task1 = new Task("Task 1", "Description", Duration.ofHours(1), now.plusHours(3));
        Task task2 = new Task("Task 2", "Description", Duration.ofHours(1), now.plusHours(1));
        Task task3 = new Task("Task 3", "Description", Duration.ofHours(1), now.plusHours(2));
        Task taskWithoutTime = new Task("Task without time", "Description");

        manager.addTask(task1);
        manager.addTask(task2);
        manager.addTask(task3);
        manager.addTask(taskWithoutTime);

        URI url = URI.create("http://localhost:8080/prioritized");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        List<Task> prioritizedTasks = gson.fromJson(response.body(),
                com.google.gson.reflect.TypeToken.getParameterized(List.class, Task.class).getType());

        assertEquals(3, prioritizedTasks.size());

        assertEquals(task2.getId(), prioritizedTasks.get(0).getId());
        assertEquals(task3.getId(), prioritizedTasks.get(1).getId());
        assertEquals(task1.getId(), prioritizedTasks.get(2).getId());
    }

    @Test
    public void testGetPrioritizedTasksWithSubtasks() throws IOException, InterruptedException {
        LocalDateTime now = LocalDateTime.now();

        Epic epic = new Epic("Epic", "Description");
        manager.addEpic(epic);

        Task task = new Task("Task", "Description", Duration.ofHours(1), now.plusHours(2));
        Subtask subtask1 = new Subtask("Subtask 1", "Description", Status.NEW, epic.getId(),
                Duration.ofHours(1), now.plusHours(1));
        Subtask subtask2 = new Subtask("Subtask 2", "Description", Status.NEW, epic.getId(),
                Duration.ofHours(1), now.plusHours(3));

        manager.addTask(task);
        manager.addSubtask(subtask1);
        manager.addSubtask(subtask2);

        URI url = URI.create("http://localhost:8080/prioritized");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        List<Task> prioritizedTasks = gson.fromJson(response.body(),
                com.google.gson.reflect.TypeToken.getParameterized(List.class, Task.class).getType());

        assertEquals(3, prioritizedTasks.size());

        assertEquals(subtask1.getId(), prioritizedTasks.get(0).getId());
        assertEquals(task.getId(), prioritizedTasks.get(1).getId());
        assertEquals(subtask2.getId(), prioritizedTasks.get(2).getId());
    }

    @Test
    public void testPrioritizedMethodNotAllowed() throws IOException, InterruptedException {
        URI url = URI.create("http://localhost:8080/prioritized");
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString("")).build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(405, response.statusCode()); // Method Not Allowed
    }

    @Test
    public void testHistoryOrderAfterMultipleViews() throws IOException, InterruptedException {
        Task task1 = new Task("Task 1", "Description 1");
        Task task2 = new Task("Task 2", "Description 2");
        Task task3 = new Task("Task 3", "Description 3");

        manager.addTask(task1);
        manager.addTask(task2);
        manager.addTask(task3);

        HttpClient client = HttpClient.newHttpClient();

        client.send(HttpRequest.newBuilder().uri(URI.create("http://localhost:8080/tasks/" + task1.getId())).GET().build(),
                HttpResponse.BodyHandlers.ofString());
        client.send(HttpRequest.newBuilder().uri(URI.create("http://localhost:8080/tasks/" + task2.getId())).GET().build(),
                HttpResponse.BodyHandlers.ofString());
        client.send(HttpRequest.newBuilder().uri(URI.create("http://localhost:8080/tasks/" + task3.getId())).GET().build(),
                HttpResponse.BodyHandlers.ofString());

        client.send(HttpRequest.newBuilder().uri(URI.create("http://localhost:8080/tasks/" + task1.getId())).GET().build(),
                HttpResponse.BodyHandlers.ofString());

        URI historyUrl = URI.create("http://localhost:8080/history");
        HttpRequest historyRequest = HttpRequest.newBuilder().uri(historyUrl).GET().build();

        HttpResponse<String> response = client.send(historyRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        List<Task> history = gson.fromJson(response.body(),
                com.google.gson.reflect.TypeToken.getParameterized(List.class, Task.class).getType());

        assertEquals(3, history.size());
        assertEquals(task2.getId(), history.get(0).getId());
        assertEquals(task3.getId(), history.get(1).getId());
        assertEquals(task1.getId(), history.get(2).getId());
    }
}