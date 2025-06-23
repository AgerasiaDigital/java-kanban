package manager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tasks.*;

import static org.junit.jupiter.api.Assertions.*;

class EpicStatusTest {
    private TaskManager taskManager;

    @BeforeEach
    void setUp() {
        taskManager = new InMemoryTaskManager();
    }

    @Test
    void epicWithAllNewSubtasks() {
        Epic epic = new Epic("Эпик", "Описание");
        taskManager.addEpic(epic);

        Subtask subtask1 = new Subtask("Подзадача 1", "Описание", Status.NEW, epic.getId());
        Subtask subtask2 = new Subtask("Подзадача 2", "Описание", Status.NEW, epic.getId());

        taskManager.addSubtask(subtask1);
        taskManager.addSubtask(subtask2);

        Epic updatedEpic = taskManager.getEpicById(epic.getId());
        assertEquals(Status.NEW, updatedEpic.getStatus());
    }

    @Test
    void epicWithAllDoneSubtasks() {
        Epic epic = new Epic("Эпик", "Описание");
        taskManager.addEpic(epic);

        Subtask subtask1 = new Subtask("Подзадача 1", "Описание", Status.DONE, epic.getId());
        Subtask subtask2 = new Subtask("Подзадача 2", "Описание", Status.DONE, epic.getId());

        taskManager.addSubtask(subtask1);
        taskManager.addSubtask(subtask2);

        Epic updatedEpic = taskManager.getEpicById(epic.getId());
        assertEquals(Status.DONE, updatedEpic.getStatus());
    }

    @Test
    void epicWithNewAndDoneSubtasks() {
        Epic epic = new Epic("Эпик", "Описание");
        taskManager.addEpic(epic);

        Subtask subtask1 = new Subtask("Подзадача 1", "Описание", Status.NEW, epic.getId());
        Subtask subtask2 = new Subtask("Подзадача 2", "Описание", Status.DONE, epic.getId());

        taskManager.addSubtask(subtask1);
        taskManager.addSubtask(subtask2);

        Epic updatedEpic = taskManager.getEpicById(epic.getId());
        assertEquals(Status.IN_PROGRESS, updatedEpic.getStatus());
    }

    @Test
    void epicWithInProgressSubtasks() {
        Epic epic = new Epic("Эпик", "Описание");
        taskManager.addEpic(epic);

        Subtask subtask1 = new Subtask("Подзадача 1", "Описание", Status.IN_PROGRESS, epic.getId());
        Subtask subtask2 = new Subtask("Подзадача 2", "Описание", Status.NEW, epic.getId());

        taskManager.addSubtask(subtask1);
        taskManager.addSubtask(subtask2);

        Epic updatedEpic = taskManager.getEpicById(epic.getId());
        assertEquals(Status.IN_PROGRESS, updatedEpic.getStatus());
    }

    @Test
    void epicWithNoSubtasks() {
        Epic epic = new Epic("Эпик", "Описание");
        taskManager.addEpic(epic);

        assertEquals(Status.NEW, epic.getStatus());
    }
}