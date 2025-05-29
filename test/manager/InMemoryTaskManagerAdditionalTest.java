package manager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tasks.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryTaskManagerAdditionalTest {
    private TaskManager taskManager;

    @BeforeEach
    void setUp() {
        taskManager = Managers.getDefault();
    }

    @Test
    void deleteTaskRemovesFromHistory() {
        Task task = new Task("Тестовая задача", "Описание");
        taskManager.addTask(task);

        taskManager.getTaskById(task.getId());
        assertEquals(1, taskManager.getHistory().size(), "Задача должна быть в истории");

        taskManager.deleteTaskById(task.getId());

        assertTrue(taskManager.getHistory().isEmpty(), "История должна быть пустой после удаления задачи");
    }

    @Test
    void deleteEpicRemovesEpicAndSubtasksFromHistory() {
        Epic epic = new Epic("Тестовый эпик", "Описание эпика");
        taskManager.addEpic(epic);

        Subtask subtask1 = new Subtask("Подзадача 1", "Описание 1", Status.NEW, epic.getId());
        Subtask subtask2 = new Subtask("Подзадача 2", "Описание 2", Status.NEW, epic.getId());
        taskManager.addSubtask(subtask1);
        taskManager.addSubtask(subtask2);

        taskManager.getEpicById(epic.getId());
        taskManager.getSubtaskById(subtask1.getId());
        taskManager.getSubtaskById(subtask2.getId());

        assertEquals(3, taskManager.getHistory().size(), "В истории должно быть 3 элемента");

        taskManager.deleteEpicById(epic.getId());

        assertTrue(taskManager.getHistory().isEmpty(), "История должна быть пустой после удаления эпика");
        assertNull(taskManager.getSubtaskById(subtask1.getId()), "Подзадача 1 должна быть удалена");
        assertNull(taskManager.getSubtaskById(subtask2.getId()), "Подзадача 2 должна быть удалена");
    }

    @Test
    void deleteSubtaskRemovesFromHistory() {
        Epic epic = new Epic("Тестовый эпик", "Описание эпика");
        taskManager.addEpic(epic);

        Subtask subtask = new Subtask("Подзадача", "Описание", Status.NEW, epic.getId());
        taskManager.addSubtask(subtask);

        taskManager.getSubtaskById(subtask.getId());
        assertEquals(1, taskManager.getHistory().size(), "Подзадача должна быть в истории");

        taskManager.deleteSubtaskById(subtask.getId());

        assertTrue(taskManager.getHistory().isEmpty(), "История должна быть пустой после удаления подзадачи");

        Epic updatedEpic = taskManager.getEpicById(epic.getId());
        assertTrue(updatedEpic.getSubtaskIds().isEmpty(), "Список подзадач эпика должен быть пустым");
    }

    @Test
    void clearTasksRemovesFromHistory() {
        Task task1 = new Task("Задача 1", "Описание 1");
        Task task2 = new Task("Задача 2", "Описание 2");
        taskManager.addTask(task1);
        taskManager.addTask(task2);

        taskManager.getTaskById(task1.getId());
        taskManager.getTaskById(task2.getId());
        assertEquals(2, taskManager.getHistory().size(), "В истории должно быть 2 задачи");

        taskManager.clearTasks();

        assertTrue(taskManager.getAllTasks().isEmpty(), "Список задач должен быть пустым");
        assertTrue(taskManager.getHistory().isEmpty(), "История должна быть пустой");
    }

    @Test
    void clearEpicsRemovesFromHistory() {
        Epic epic = new Epic("Эпик", "Описание");
        taskManager.addEpic(epic);

        Subtask subtask = new Subtask("Подзадача", "Описание", Status.NEW, epic.getId());
        taskManager.addSubtask(subtask);

        taskManager.getEpicById(epic.getId());
        taskManager.getSubtaskById(subtask.getId());
        assertEquals(2, taskManager.getHistory().size(), "В истории должно быть 2 элемента");

        taskManager.clearEpics();

        assertTrue(taskManager.getAllEpics().isEmpty(), "Список эпиков должен быть пустым");
        assertTrue(taskManager.getAllSubtasks().isEmpty(), "Список подзадач должен быть пустым");
        assertTrue(taskManager.getHistory().isEmpty(), "История должна быть пустой");
    }

    @Test
    void clearSubtasksRemovesFromHistory() {
        Epic epic = new Epic("Эпик", "Описание");
        taskManager.addEpic(epic);

        Subtask subtask1 = new Subtask("Подзадача 1", "Описание 1", Status.NEW, epic.getId());
        Subtask subtask2 = new Subtask("Подзадача 2", "Описание 2", Status.NEW, epic.getId());
        taskManager.addSubtask(subtask1);
        taskManager.addSubtask(subtask2);

        taskManager.getEpicById(epic.getId());
        taskManager.getSubtaskById(subtask1.getId());
        taskManager.getSubtaskById(subtask2.getId());
        assertEquals(3, taskManager.getHistory().size(), "В истории должно быть 3 элемента");

        taskManager.clearSubtasks();

        assertTrue(taskManager.getAllSubtasks().isEmpty(), "Список подзадач должен быть пустым");

        List<Task> history = taskManager.getHistory();
        assertEquals(1, history.size(), "В истории должен остаться только эпик");
        assertEquals(epic, history.get(0), "В истории должен быть только эпик");

        Epic updatedEpic = taskManager.getEpicById(epic.getId());
        assertTrue(updatedEpic.getSubtaskIds().isEmpty(), "Список подзадач эпика должен быть пустым");
        assertEquals(Status.NEW, updatedEpic.getStatus(), "Статус эпика должен быть NEW");
    }

    @Test
    void historyNoDuplicatesAfterMultipleViews() {
        Task task = new Task("Задача", "Описание");
        taskManager.addTask(task);

        taskManager.getTaskById(task.getId());
        taskManager.getTaskById(task.getId());
        taskManager.getTaskById(task.getId());

        List<Task> history = taskManager.getHistory();
        assertEquals(1, history.size(), "В истории должна быть только одна задача");
        assertEquals(task, history.get(0), "В истории должна быть та же задача");
    }

    @Test
    void historyMaintainsOrderWithDuplicates() {
        Task task1 = new Task("Задача 1", "Описание 1");
        Task task2 = new Task("Задача 2", "Описание 2");
        Epic epic = new Epic("Эпик", "Описание эпика");

        taskManager.addTask(task1);
        taskManager.addTask(task2);
        taskManager.addEpic(epic);

        taskManager.getTaskById(task1.getId());  // [task1]
        taskManager.getTaskById(task2.getId());  // [task1, task2]
        taskManager.getEpicById(epic.getId());   // [task1, task2, epic]
        taskManager.getTaskById(task1.getId());  // [task2, epic, task1] - task1 переместился в конец

        List<Task> history = taskManager.getHistory();
        assertEquals(3, history.size(), "В истории должно быть 3 элемента");
        assertEquals(task2, history.get(0), "task2 должна быть первой");
        assertEquals(epic, history.get(1), "epic должен быть вторым");
        assertEquals(task1, history.get(2), "task1 должна быть последней");
    }

    @Test
    void epicStatusUpdatesCorrectlyAfterSubtaskDeletion() {
        Epic epic = new Epic("Эпик", "Описание");
        taskManager.addEpic(epic);

        Subtask subtask1 = new Subtask("Подзадача 1", "Описание 1", Status.NEW, epic.getId());
        Subtask subtask2 = new Subtask("Подзадача 2", "Описание 2", Status.DONE, epic.getId());
        taskManager.addSubtask(subtask1);
        taskManager.addSubtask(subtask2);

        assertEquals(Status.IN_PROGRESS, taskManager.getEpicById(epic.getId()).getStatus());

        taskManager.deleteSubtaskById(subtask2.getId());

        assertEquals(Status.NEW, taskManager.getEpicById(epic.getId()).getStatus());

        taskManager.deleteSubtaskById(subtask1.getId());

        assertEquals(Status.NEW, taskManager.getEpicById(epic.getId()).getStatus());
    }

    @Test
    void taskUpdateDoesNotBreakHistory() {
        Task task = new Task("Оригинальное название", "Оригинальное описание");
        taskManager.addTask(task);

        taskManager.getTaskById(task.getId());
        assertEquals(1, taskManager.getHistory().size());

        task.setName("Новое название");
        task.setStatus(Status.IN_PROGRESS);
        taskManager.updateTask(task);

        List<Task> history = taskManager.getHistory();
        assertEquals(1, history.size(), "История должна содержать задачу");

        Task taskFromHistory = history.get(0);
        assertEquals("Новое название", taskFromHistory.getName(), "Название в истории должно обновиться");
        assertEquals(Status.IN_PROGRESS, taskFromHistory.getStatus(), "Статус в истории должен обновиться");
    }

    @Test
    void subtaskCannotBeAddedToNonExistentEpic() {
        Subtask subtask = new Subtask("Подзадача", "Описание", Status.NEW, 999); // Несуществующий эпик

        taskManager.addSubtask(subtask);

        assertTrue(taskManager.getAllSubtasks().isEmpty(), "Подзадача не должна быть добавлена");
        assertEquals(0, subtask.getId(), "ID подзадачи не должен быть установлен");
    }

    @Test
    void updateNonExistentTaskDoesNothing() {
        Task task = new Task("Задача", "Описание");
        task.setId(999); // Несуществующий ID

        taskManager.updateTask(task);

        assertNull(taskManager.getTaskById(999), "Задача не должна существовать");
        assertTrue(taskManager.getAllTasks().isEmpty(), "Список задач должен быть пустым");
    }

    @Test
    void deleteNonExistentTaskDoesNothing() {
        Task task = new Task("Задача", "Описание");
        taskManager.addTask(task);

        taskManager.getTaskById(task.getId());
        assertEquals(1, taskManager.getHistory().size());

        taskManager.deleteTaskById(999);

        assertNotNull(taskManager.getTaskById(task.getId()), "Существующая задача должна остаться");
        assertEquals(1, taskManager.getHistory().size(), "История не должна измениться");
    }
}