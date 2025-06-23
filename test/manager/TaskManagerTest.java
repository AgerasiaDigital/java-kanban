package manager;

import org.junit.jupiter.api.Test;
import tasks.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

abstract class TaskManagerTest<T extends TaskManager> {
    protected T taskManager;

    protected abstract T createTaskManager();

    @Test
    void addTask() {
        Task task = new Task("Тестовая задача", "Описание тестовой задачи");
        taskManager.addTask(task);

        Task savedTask = taskManager.getTaskById(task.getId());

        assertNotNull(savedTask);
        assertEquals(task, savedTask);
        assertEquals("Тестовая задача", savedTask.getName());
        assertEquals("Описание тестовой задачи", savedTask.getDescription());
        assertEquals(Status.NEW, savedTask.getStatus());
    }

    @Test
    void addEpic() {
        Epic epic = new Epic("Тестовый эпик", "Описание тестового эпика");
        taskManager.addEpic(epic);

        Epic savedEpic = taskManager.getEpicById(epic.getId());

        assertNotNull(savedEpic);
        assertEquals(epic, savedEpic);
        assertEquals("Тестовый эпик", savedEpic.getName());
        assertEquals("Описание тестового эпика", savedEpic.getDescription());
        assertEquals(Status.NEW, savedEpic.getStatus());
        assertTrue(savedEpic.getSubtaskIds().isEmpty());
    }

    @Test
    void addSubtask() {
        Epic epic = new Epic("Тестовый эпик", "Описание тестового эпика");
        taskManager.addEpic(epic);

        Subtask subtask = new Subtask("Тестовая подзадача", "Описание тестовой подзадачи", Status.NEW, epic.getId());
        taskManager.addSubtask(subtask);

        Subtask savedSubtask = taskManager.getSubtaskById(subtask.getId());

        assertNotNull(savedSubtask);
        assertEquals(subtask, savedSubtask);
        assertEquals("Тестовая подзадача", savedSubtask.getName());
        assertEquals("Описание тестовой подзадачи", savedSubtask.getDescription());
        assertEquals(Status.NEW, savedSubtask.getStatus());
        assertEquals(epic.getId(), savedSubtask.getEpicId());

        Epic updatedEpic = taskManager.getEpicById(epic.getId());
        assertTrue(updatedEpic.getSubtaskIds().contains(subtask.getId()));
    }

    @Test
    void updateTaskStatus() {
        Task task = new Task("Тестовая задача", "Описание тестовой задачи");
        taskManager.addTask(task);

        task.setStatus(Status.IN_PROGRESS);
        taskManager.updateTask(task);

        Task updatedTask = taskManager.getTaskById(task.getId());
        assertEquals(Status.IN_PROGRESS, updatedTask.getStatus());
    }

    @Test
    void updateEpicStatusAllNew() {
        Epic epic = new Epic("Тестовый эпик", "Описание тестового эпика");
        taskManager.addEpic(epic);

        Subtask subtask1 = new Subtask("Подзадача 1", "Описание подзадачи 1", Status.NEW, epic.getId());
        Subtask subtask2 = new Subtask("Подзадача 2", "Описание подзадачи 2", Status.NEW, epic.getId());
        taskManager.addSubtask(subtask1);
        taskManager.addSubtask(subtask2);

        Epic epicWithNewSubtasks = taskManager.getEpicById(epic.getId());
        assertEquals(Status.NEW, epicWithNewSubtasks.getStatus());
    }

    @Test
    void updateEpicStatusAllDone() {
        Epic epic = new Epic("Тестовый эпик", "Описание тестового эпика");
        taskManager.addEpic(epic);

        Subtask subtask1 = new Subtask("Подзадача 1", "Описание подзадачи 1", Status.DONE, epic.getId());
        Subtask subtask2 = new Subtask("Подзадача 2", "Описание подзадачи 2", Status.DONE, epic.getId());
        taskManager.addSubtask(subtask1);
        taskManager.addSubtask(subtask2);

        Epic epicWithDoneSubtasks = taskManager.getEpicById(epic.getId());
        assertEquals(Status.DONE, epicWithDoneSubtasks.getStatus());
    }

    @Test
    void updateEpicStatusNewAndDone() {
        Epic epic = new Epic("Тестовый эпик", "Описание тестового эпика");
        taskManager.addEpic(epic);

        Subtask subtask1 = new Subtask("Подзадача 1", "Описание подзадачи 1", Status.NEW, epic.getId());
        Subtask subtask2 = new Subtask("Подзадача 2", "Описание подзадачи 2", Status.DONE, epic.getId());
        taskManager.addSubtask(subtask1);
        taskManager.addSubtask(subtask2);

        Epic epicWithMixedSubtasks = taskManager.getEpicById(epic.getId());
        assertEquals(Status.IN_PROGRESS, epicWithMixedSubtasks.getStatus());
    }

    @Test
    void updateEpicStatusInProgress() {
        Epic epic = new Epic("Тестовый эпик", "Описание тестового эпика");
        taskManager.addEpic(epic);

        Subtask subtask1 = new Subtask("Подзадача 1", "Описание подзадачи 1", Status.IN_PROGRESS, epic.getId());
        taskManager.addSubtask(subtask1);

        Epic epicWithInProgressSubtask = taskManager.getEpicById(epic.getId());
        assertEquals(Status.IN_PROGRESS, epicWithInProgressSubtask.getStatus());
    }

    @Test
    void deleteTask() {
        Task task = new Task("Тестовая задача", "Описание тестовой задачи");
        taskManager.addTask(task);

        taskManager.deleteTaskById(task.getId());

        assertNull(taskManager.getTaskById(task.getId()));
        assertTrue(taskManager.getAllTasks().isEmpty());
    }

    @Test
    void deleteEpicWithSubtasks() {
        Epic epic = new Epic("Тестовый эпик", "Описание тестового эпика");
        taskManager.addEpic(epic);

        Subtask subtask = new Subtask("Тестовая подзадача", "Описание тестовой подзадачи", Status.NEW, epic.getId());
        taskManager.addSubtask(subtask);

        taskManager.deleteEpicById(epic.getId());

        assertNull(taskManager.getEpicById(epic.getId()));
        assertNull(taskManager.getSubtaskById(subtask.getId()));
        assertTrue(taskManager.getAllEpics().isEmpty());
        assertTrue(taskManager.getAllSubtasks().isEmpty());
    }

    @Test
    void getHistory() {
        Task task = new Task("Тестовая задача", "Описание тестовой задачи");
        Epic epic = new Epic("Тестовый эпик", "Описание тестового эпика");

        taskManager.addTask(task);
        taskManager.addEpic(epic);

        assertTrue(taskManager.getHistory().isEmpty());

        taskManager.getTaskById(task.getId());
        taskManager.getEpicById(epic.getId());

        List<Task> history = taskManager.getHistory();

        assertEquals(2, history.size());
        assertEquals(task, history.get(0));
        assertEquals(epic, history.get(1));
    }

    @Test
    void getPrioritizedTasks() {
        LocalDateTime now = LocalDateTime.now();

        Task task1 = new Task("Задача 1", "Описание", Duration.ofHours(1), now.plusHours(2));
        Task task2 = new Task("Задача 2", "Описание", Duration.ofHours(1), now.plusHours(1));
        Task task3 = new Task("Задача 3", "Описание");

        taskManager.addTask(task1);
        taskManager.addTask(task2);
        taskManager.addTask(task3);

        List<Task> prioritizedTasks = taskManager.getPrioritizedTasks();

        assertEquals(2, prioritizedTasks.size());
        assertEquals(task2, prioritizedTasks.get(0));
        assertEquals(task1, prioritizedTasks.get(1));
    }

    @Test
    void checkTimeConflict() {
        LocalDateTime now = LocalDateTime.now();

        Task task1 = new Task("Задача 1", "Описание", Duration.ofHours(2), now);
        taskManager.addTask(task1);

        Task conflictingTask = new Task("Конфликтующая задача", "Описание", Duration.ofHours(1), now.plusMinutes(30));

        assertThrows(IllegalArgumentException.class, () -> {
            taskManager.addTask(conflictingTask);
        });
    }

    @Test
    void noTimeConflictWhenNoTime() {
        Task task1 = new Task("Задача 1", "Описание");
        Task task2 = new Task("Задача 2", "Описание");

        taskManager.addTask(task1);
        taskManager.addTask(task2);

        assertEquals(2, taskManager.getAllTasks().size());
    }

    @Test
    void epicTimeCalculation() {
        Epic epic = new Epic("Эпик", "Описание");
        taskManager.addEpic(epic);

        LocalDateTime now = LocalDateTime.now();

        Subtask subtask1 = new Subtask("Подзадача 1", "Описание", Status.NEW, epic.getId(),
                Duration.ofHours(1), now);
        Subtask subtask2 = new Subtask("Подзадача 2", "Описание", Status.NEW, epic.getId(),
                Duration.ofHours(2), now.plusHours(2));

        taskManager.addSubtask(subtask1);
        taskManager.addSubtask(subtask2);

        Epic updatedEpic = taskManager.getEpicById(epic.getId());

        assertEquals(now, updatedEpic.getStartTime());
        assertEquals(now.plusHours(4), updatedEpic.getEndTime());
        assertEquals(Duration.ofHours(3), updatedEpic.getDuration());
    }
}