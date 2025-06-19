package manager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tasks.*;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class TimeOverlapTest {
    private TaskManager taskManager;
    private LocalDateTime baseTime;

    @BeforeEach
    void setUp() {
        taskManager = new InMemoryTaskManager();
        baseTime = LocalDateTime.of(2024, 1, 1, 10, 0);
    }

    @Test
    void noOverlapWhenTasksAreSequential() {
        Task task1 = new Task("Задача 1", "Описание", Duration.ofHours(1), baseTime);
        Task task2 = new Task("Задача 2", "Описание", Duration.ofHours(1), baseTime.plusHours(1));

        taskManager.addTask(task1);
        taskManager.addTask(task2);

        assertEquals(2, taskManager.getAllTasks().size());
    }

    @Test
    void overlapWhenTasksIntersect() {
        Task task1 = new Task("Задача 1", "Описание", Duration.ofHours(2), baseTime);
        taskManager.addTask(task1);

        Task task2 = new Task("Задача 2", "Описание", Duration.ofHours(1), baseTime.plusMinutes(30));

        assertThrows(IllegalArgumentException.class, () -> {
            taskManager.addTask(task2);
        });
    }

    @Test
    void overlapWhenOneTaskContainsAnother() {
        Task task1 = new Task("Задача 1", "Описание", Duration.ofHours(4), baseTime);
        taskManager.addTask(task1);

        Task task2 = new Task("Задача 2", "Описание", Duration.ofHours(1), baseTime.plusHours(1));

        assertThrows(IllegalArgumentException.class, () -> {
            taskManager.addTask(task2);
        });
    }

    @Test
    void overlapWhenTasksStartAtSameTime() {
        Task task1 = new Task("Задача 1", "Описание", Duration.ofHours(1), baseTime);
        taskManager.addTask(task1);

        Task task2 = new Task("Задача 2", "Описание", Duration.ofHours(2), baseTime);

        assertThrows(IllegalArgumentException.class, () -> {
            taskManager.addTask(task2);
        });
    }

    @Test
    void noOverlapWhenNoTimeSet() {
        Task task1 = new Task("Задача 1", "Описание");
        Task task2 = new Task("Задача 2", "Описание");

        taskManager.addTask(task1);
        taskManager.addTask(task2);

        assertEquals(2, taskManager.getAllTasks().size());
    }

    @Test
    void overlapCheckForSubtasks() {
        Epic epic = new Epic("Эпик", "Описание");
        taskManager.addEpic(epic);

        Subtask subtask1 = new Subtask("Подзадача 1", "Описание", Status.NEW, epic.getId(),
                Duration.ofHours(1), baseTime);
        taskManager.addSubtask(subtask1);

        Subtask subtask2 = new Subtask("Подзадача 2", "Описание", Status.NEW, epic.getId(),
                Duration.ofMinutes(30), baseTime.plusMinutes(30));

        assertThrows(IllegalArgumentException.class, () -> {
            taskManager.addSubtask(subtask2);
        });
    }

    @Test
    void overlapCheckOnUpdate() {
        Task task1 = new Task("Задача 1", "Описание", Duration.ofHours(1), baseTime);
        Task task2 = new Task("Задача 2", "Описание", Duration.ofHours(1), baseTime.plusHours(2));

        taskManager.addTask(task1);
        taskManager.addTask(task2);

        task2.setStartTime(baseTime.plusMinutes(30));

        assertThrows(IllegalArgumentException.class, () -> {
            taskManager.updateTask(task2);
        });
    }

    @Test
    void noOverlapAfterTaskDeletion() {
        Task task1 = new Task("Задача 1", "Описание", Duration.ofHours(2), baseTime);
        taskManager.addTask(task1);

        taskManager.deleteTaskById(task1.getId());

        Task task2 = new Task("Задача 2", "Описание", Duration.ofHours(1), baseTime.plusMinutes(30));
        taskManager.addTask(task2);

        assertEquals(1, taskManager.getAllTasks().size());
    }
}