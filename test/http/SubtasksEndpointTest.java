package http;

import org.junit.jupiter.api.Test;
import tasks.Epic;
import tasks.Status;
import tasks.Subtask;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class SubtasksEndpointTest extends BaseHttpEndpointTest {

    @Test
    public void testCreateSubtask() throws IOException, InterruptedException {
        String subtaskJson = gson.toJson(testSubtask);

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
        assertEquals(testEpic.getId(), subtasksFromManager.get(0).getEpicId());
    }

    @Test
    public void testGetAllSubtasks() throws IOException, InterruptedException {
        addTestTasksToManager();

        URI url = URI.create("http://localhost:8080/subtasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        List<Subtask> subtasks = gson.fromJson(response.body(),
                com.google.gson.reflect.TypeToken.getParameterized(List.class, Subtask.class).getType());
        assertEquals(1, subtasks.size());
    }

    @Test
    public void testGetSubtaskById() throws IOException, InterruptedException {
        manager.addSubtask(testSubtask);

        URI url = URI.create("http://localhost:8080/subtasks/" + testSubtask.getId());
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        Subtask receivedSubtask = gson.fromJson(response.body(), Subtask.class);
        assertEquals(testSubtask.getName(), receivedSubtask.getName());
        assertEquals(testSubtask.getId(), receivedSubtask.getId());
        assertEquals(testSubtask.getEpicId(), receivedSubtask.getEpicId());
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
        manager.addSubtask(testSubtask);

        testSubtask.setName("Updated Subtask");
        testSubtask.setStatus(Status.IN_PROGRESS);
        String subtaskJson = gson.toJson(testSubtask);

        URI url = URI.create("http://localhost:8080/subtasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(subtaskJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());

        Subtask updatedSubtask = manager.getSubtaskById(testSubtask.getId());
        assertEquals("Updated Subtask", updatedSubtask.getName());
        assertEquals(Status.IN_PROGRESS, updatedSubtask.getStatus());
    }

    @Test
    public void testDeleteSubtask() throws IOException, InterruptedException {
        manager.addSubtask(testSubtask);

        URI url = URI.create("http://localhost:8080/subtasks/" + testSubtask.getId());
        HttpRequest request = HttpRequest.newBuilder().uri(url).DELETE().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        assertTrue(manager.getAllSubtasks().isEmpty());

        Epic updatedEpic = manager.getEpicById(testEpic.getId());
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
        manager.addSubtask(testSubtask);

        Subtask conflictingSubtask = new Subtask("Conflicting Subtask", "Description",
                Status.NEW, testEpic.getId(), Duration.ofHours(1), testStartTime.plusMinutes(30));
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
        assertEquals(404, response.statusCode());

        assertTrue(manager.getAllSubtasks().isEmpty());
    }
}