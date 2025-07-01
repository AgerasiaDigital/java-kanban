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

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class SubtasksEndpointTest {

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
    public void testCreateSubtask() throws IOException, InterruptedException {
        Epic epic = new Epic("Test Epic", "Description");
        manager.addEpic(epic);

        Subtask subtask = new Subtask("Test Subtask", "Subtask description",
                Status.NEW, epic.getId());
        String subtaskJson = gson.toJson(subtask);

        URI url = URI.create("http://localhost:8080/subtasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(subtaskJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());

        List<Subtask> subtasksFromManager = manager.getAllSubtasks();
        assertEquals(1, subtasksFromManager.size());
        assertEquals("Test Subtask", subtasksFromManager.get(0).getName());
        assertEquals(epic.getId(), subtasksFromManager.get(0).getEpicId());
    }

    @Test
    public void testGetAllSubtasks() throws IOException, InterruptedException {
        Epic epic = new Epic("Epic", "Description");
        manager.addEpic(epic);

        Subtask subtask1 = new Subtask("Subtask 1", "Description 1", Status.NEW, epic.getId());
        Subtask subtask2 = new Subtask("Subtask 2", "Description 2", Status.NEW, epic.getId());
        manager.addSubtask(subtask1);
        manager.addSubtask(subtask2);

        URI url = URI.create("http://localhost:8080/subtasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        List<Subtask> subtasks = gson.fromJson(response.body(),
                com.google.gson.reflect.TypeToken.getParameterized(List.class, Subtask.class).getType());
        assertEquals(2, subtasks.size());
    }

    @Test
    public void testGetSubtaskById() throws IOException, InterruptedException {
        Epic epic = new Epic("Epic", "Description");
        manager.addEpic(epic);

        Subtask subtask = new Subtask("Test Subtask", "Description", Status.NEW, epic.getId());
        manager.addSubtask(subtask);

        URI url = URI.create("http://localhost:8080/subtasks/" + subtask.getId());
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        Subtask receivedSubtask = gson.fromJson(response.body(), Subtask.class);
        assertEquals(subtask.getName(), receivedSubtask.getName());
        assertEquals(subtask.getId(), receivedSubtask.getId());
        assertEquals(subtask.getEpicId(), receivedSubtask.getEpicId());
    }

    @Test
    public void testGetSubtaskByIdNotFound() throws IOException, InterruptedException {
        URI url = URI.create("http://localhost:8080/subtasks/999");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, response.statusCode());
    }

    @Test
    public void testUpdateSubtask() throws IOException, InterruptedException {
        Epic epic = new Epic("Epic", "Description");
        manager.addEpic(epic);

        Subtask subtask = new Subtask("Original Subtask", "Original Description",
                Status.NEW, epic.getId());
        manager.addSubtask(subtask);

        subtask.setName("Updated Subtask");
        subtask.setStatus(Status.IN_PROGRESS);
        String subtaskJson = gson.toJson(subtask);

        URI url = URI.create("http://localhost:8080/subtasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(subtaskJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());

        Subtask updatedSubtask = manager.getSubtaskById(subtask.getId());
        assertEquals("Updated Subtask", updatedSubtask.getName());
        assertEquals(Status.IN_PROGRESS, updatedSubtask.getStatus());
    }

    @Test
    public void testDeleteSubtask() throws IOException, InterruptedException {
        Epic epic = new Epic("Epic", "Description");
        manager.addEpic(epic);

        Subtask subtask = new Subtask("Subtask to delete", "Description", Status.NEW, epic.getId());
        manager.addSubtask(subtask);

        URI url = URI.create("http://localhost:8080/subtasks/" + subtask.getId());
        HttpRequest request = HttpRequest.newBuilder().uri(url).DELETE().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        assertTrue(manager.getAllSubtasks().isEmpty());

        Epic updatedEpic = manager.getEpicById(epic.getId());
        assertTrue(updatedEpic.getSubtaskIds().isEmpty());
    }

    @Test
    public void testDeleteSubtaskNotFound() throws IOException, InterruptedException {
        URI url = URI.create("http://localhost:8080/subtasks/999");
        HttpRequest request = HttpRequest.newBuilder().uri(url).DELETE().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, response.statusCode());
    }

    @Test
    public void testSubtaskTimeConflict() throws IOException, InterruptedException {
        Epic epic = new Epic("Epic", "Description");
        manager.addEpic(epic);

        LocalDateTime startTime = LocalDateTime.now();
        Subtask subtask1 = new Subtask("Subtask 1", "Description", Status.NEW, epic.getId(),
                Duration.ofHours(2), startTime);
        manager.addSubtask(subtask1);

        Subtask conflictingSubtask = new Subtask("Conflicting Subtask", "Description",
                Status.NEW, epic.getId(), Duration.ofHours(1), startTime.plusMinutes(30));
        String subtaskJson = gson.toJson(conflictingSubtask);

        URI url = URI.create("http://localhost:8080/subtasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(subtaskJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(406, response.statusCode());
    }

    @Test
    public void testCreateSubtaskWithNonExistentEpic() throws IOException, InterruptedException {
        Subtask subtask = new Subtask("Test Subtask", "Description", Status.NEW, 999);
        String subtaskJson = gson.toJson(subtask);

        URI url = URI.create("http://localhost:8080/subtasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(subtaskJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(500, response.statusCode()); // Внутренняя ошибка, так как эпик не существует

        assertTrue(manager.getAllSubtasks().isEmpty());
    }
}