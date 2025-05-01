package manager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tasks.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryTaskManagerTest {
    private TaskManager taskManager;

    @BeforeEach
    void setUp() {
        taskManager = Managers.getDefault();
    }

    @Test
    void addTask() {
        Task task = new Task("Тестовая задача", "Описание тестовой задачи");
        taskManager.addTask(task);

        Task savedTask = taskManager.getTaskById(task.getId());

        assertNotNull(savedTask, "Задача не найдена");
        assertEquals(task, savedTask, "Задачи не совпадают");
        assertEquals("Тестовая задача", savedTask.getName(), "Название задачи не совпадает");
        assertEquals("Описание тестовой задачи", savedTask.getDescription(), "Описание задачи не совпадает");
        assertEquals(Status.NEW, savedTask.getStatus(), "Статус задачи не совпадает");
    }

    @Test
    void addEpic() {
        Epic epic = new Epic("Тестовый эпик", "Описание тестового эпика");
        taskManager.addEpic(epic);

        Epic savedEpic = taskManager.getEpicById(epic.getId());

        assertNotNull(savedEpic, "Эпик не найден");
        assertEquals(epic, savedEpic, "Эпики не совпадают");
        assertEquals("Тестовый эпик", savedEpic.getName(), "Название эпика не совпадает");
        assertEquals("Описание тестового эпика", savedEpic.getDescription(), "Описание эпика не совпадает");
        assertEquals(Status.NEW, savedEpic.getStatus(), "Статус эпика не совпадает");
        assertTrue(savedEpic.getSubtaskIds().isEmpty(), "Список подзадач должен быть пустым");
    }

    @Test
    void addSubtask() {
        Epic epic = new Epic("Тестовый эпик", "Описание тестового эпика");
        taskManager.addEpic(epic);

        Subtask subtask = new Subtask("Тестовая подзадача", "Описание тестовой подзадачи", Status.NEW, epic.getId());
        taskManager.addSubtask(subtask);

        Subtask savedSubtask = taskManager.getSubtaskById(subtask.getId());

        assertNotNull(savedSubtask, "Подзадача не найдена");
        assertEquals(subtask, savedSubtask, "Подзадачи не совпадают");
        assertEquals("Тестовая подзадача", savedSubtask.getName(), "Название подзадачи не совпадает");
        assertEquals("Описание тестовой подзадачи", savedSubtask.getDescription(), "Описание подзадачи не совпадает");
        assertEquals(Status.NEW, savedSubtask.getStatus(), "Статус подзадачи не совпадает");
        assertEquals(epic.getId(), savedSubtask.getEpicId(), "ID эпика не совпадает");

        Epic updatedEpic = taskManager.getEpicById(epic.getId());
        assertTrue(updatedEpic.getSubtaskIds().contains(subtask.getId()), "Подзадача должна быть добавлена в эпик");
    }

    @Test
    void updateTaskStatus() {
        Task task = new Task("Тестовая задача", "Описание тестовой задачи");
        taskManager.addTask(task);

        task.setStatus(Status.IN_PROGRESS);
        taskManager.updateTask(task);

        Task updatedTask = taskManager.getTaskById(task.getId());

        assertEquals(Status.IN_PROGRESS, updatedTask.getStatus(), "Статус задачи должен быть обновлен");
    }

    @Test
    void updateEpicStatusBasedOnSubtasks() {
        Epic epic = new Epic("Тестовый эпик", "Описание тестового эпика");
        taskManager.addEpic(epic);

        Subtask subtask1 = new Subtask("Подзадача 1", "Описание подзадачи 1", Status.NEW, epic.getId());
        Subtask subtask2 = new Subtask("Подзадача 2", "Описание подзадачи 2", Status.NEW, epic.getId());
        taskManager.addSubtask(subtask1);
        taskManager.addSubtask(subtask2);

        Epic epicWithNewSubtasks = taskManager.getEpicById(epic.getId());
        assertEquals(Status.NEW, epicWithNewSubtasks.getStatus(), "Статус эпика должен быть NEW, когда все подзадачи NEW");

        subtask1.setStatus(Status.DONE);
        taskManager.updateSubtask(subtask1);

        Epic epicWithMixedSubtasks = taskManager.getEpicById(epic.getId());
        assertEquals(Status.IN_PROGRESS, epicWithMixedSubtasks.getStatus(), "Статус эпика должен быть IN_PROGRESS, когда подзадачи имеют разные статусы");

        subtask2.setStatus(Status.DONE);
        taskManager.updateSubtask(subtask2);

        Epic epicWithDoneSubtasks = taskManager.getEpicById(epic.getId());
        assertEquals(Status.DONE, epicWithDoneSubtasks.getStatus(), "Статус эпика должен быть DONE, когда все подзадачи DONE");
    }

    @Test
    void deleteTask() {
        Task task = new Task("Тестовая задача", "Описание тестовой задачи");
        taskManager.addTask(task);

        taskManager.deleteTaskById(task.getId());

        assertNull(taskManager.getTaskById(task.getId()), "Задача должна быть удалена");
        assertTrue(taskManager.getAllTasks().isEmpty(), "Список задач должен быть пустым");
    }

    @Test
    void deleteEpicWithSubtasks() {
        Epic epic = new Epic("Тестовый эпик", "Описание тестового эпика");
        taskManager.addEpic(epic);

        Subtask subtask = new Subtask("Тестовая подзадача", "Описание тестовой подзадачи", Status.NEW, epic.getId());
        taskManager.addSubtask(subtask);

        taskManager.deleteEpicById(epic.getId());

        assertNull(taskManager.getEpicById(epic.getId()), "Эпик должен быть удален");
        assertNull(taskManager.getSubtaskById(subtask.getId()), "Подзадачи эпика должны быть удалены");
        assertTrue(taskManager.getAllEpics().isEmpty(), "Список эпиков должен быть пустым");
        assertTrue(taskManager.getAllSubtasks().isEmpty(), "Список подзадач должен быть пустым");
    }

    @Test
    void getHistory() {
        Task task = new Task("Тестовая задача", "Описание тестовой задачи");
        Epic epic = new Epic("Тестовый эпик", "Описание тестового эпика");

        taskManager.addTask(task);
        taskManager.addEpic(epic);

        assertTrue(taskManager.getHistory().isEmpty(), "История должна быть пустой");

        taskManager.getTaskById(task.getId());
        taskManager.getEpicById(epic.getId());

        List<Task> history = taskManager.getHistory();

        assertEquals(2, history.size(), "История должна содержать 2 элемента");
        assertEquals(task, history.get(0), "Первым элементом истории должна быть задача");
        assertEquals(epic, history.get(1), "Вторым элементом истории должен быть эпик");
    }

    @Test
    void historyLimitedToTenItems() {
        Task[] tasks = new Task[11];
        for (int i = 0; i < 11; i++) {
            tasks[i] = new Task("Задача " + i, "Описание задачи " + i);
            taskManager.addTask(tasks[i]);
        }

        for (Task task : tasks) {
            taskManager.getTaskById(task.getId());
        }

        List<Task> history = taskManager.getHistory();

        assertEquals(10, history.size(), "История должна содержать не более 10 элементов");
        assertEquals(tasks[1], history.get(0), "Самая старая задача должна быть удалена из истории");
        assertEquals(tasks[10], history.get(9), "Последняя задача должна быть в истории");
    }

    @Test
    void taskEquality() {
        Task task1 = new Task("Задача", "Описание");
        task1.setId(1);

        Task task2 = new Task("Другая задача", "Другое описание");
        task2.setId(1);

        assertEquals(task1, task2, "Задачи с одинаковым ID должны быть равны");
        assertEquals(task1.hashCode(), task2.hashCode(), "Хеш-коды задач с одинаковым ID должны быть равны");
    }

    @Test
    void epicSubtaskEquality() {
        Epic epic = new Epic("Эпик", "Описание эпика");
        epic.setId(1);

        Subtask subtask = new Subtask("Подзадача", "Описание подзадачи", Status.NEW, 2);
        subtask.setId(1);

        assertEquals(epic, subtask, "Задачи разных типов с одинаковым ID должны быть равны");
        assertEquals(epic.hashCode(), subtask.hashCode(), "Хеш-коды задач разных типов с одинаковым ID должны быть равны");
    }
}