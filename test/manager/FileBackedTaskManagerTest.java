package manager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import tasks.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FileBackedTaskManagerTest {

    @TempDir
    Path tempDir;

    private File file;
    private FileBackedTaskManager manager;

    @BeforeEach
    void setUp() throws IOException {
        file = Files.createTempFile(tempDir, "test", ".csv").toFile();
        manager = new FileBackedTaskManager(file);
    }

    @Test
    void saveAndLoadEmptyManager() {
        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(file);

        assertTrue(loadedManager.getAllTasks().isEmpty(), "Список задач должен быть пустым");
        assertTrue(loadedManager.getAllEpics().isEmpty(), "Список эпиков должен быть пустым");
        assertTrue(loadedManager.getAllSubtasks().isEmpty(), "Список подзадач должен быть пустым");
    }

    @Test
    void saveAndLoadSingleTask() {
        Task task = new Task("Тестовая задача", "Описание задачи");
        manager.addTask(task);

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(file);

        List<Task> loadedTasks = loadedManager.getAllTasks();
        assertEquals(1, loadedTasks.size(), "Должна быть загружена одна задача");

        Task loadedTask = loadedTasks.get(0);
        assertEquals(task.getId(), loadedTask.getId(), "ID задачи должен совпадать");
        assertEquals(task.getName(), loadedTask.getName(), "Название задачи должно совпадать");
        assertEquals(task.getDescription(), loadedTask.getDescription(), "Описание задачи должно совпадать");
        assertEquals(task.getStatus(), loadedTask.getStatus(), "Статус задачи должен совпадать");
    }

    @Test
    void saveAndLoadMultipleTasks() {
        Task task1 = new Task("Задача 1", "Описание 1");
        Task task2 = new Task("Задача 2", "Описание 2");
        task2.setStatus(Status.IN_PROGRESS);

        manager.addTask(task1);
        manager.addTask(task2);

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(file);

        assertEquals(2, loadedManager.getAllTasks().size(), "Должно быть загружено 2 задачи");

        Task loadedTask1 = loadedManager.getTaskById(task1.getId());
        Task loadedTask2 = loadedManager.getTaskById(task2.getId());

        assertEquals(task1.getName(), loadedTask1.getName());
        assertEquals(task2.getName(), loadedTask2.getName());
        assertEquals(Status.NEW, loadedTask1.getStatus());
        assertEquals(Status.IN_PROGRESS, loadedTask2.getStatus());
    }

    @Test
    void saveAndLoadEpicWithSubtasks() {
        Epic epic = new Epic("Тестовый эпик", "Описание эпика");
        manager.addEpic(epic);

        Subtask subtask1 = new Subtask("Подзадача 1", "Описание 1", Status.NEW, epic.getId());
        Subtask subtask2 = new Subtask("Подзадача 2", "Описание 2", Status.DONE, epic.getId());

        manager.addSubtask(subtask1);
        manager.addSubtask(subtask2);

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(file);

        assertEquals(1, loadedManager.getAllEpics().size(), "Должен быть загружен один эпик");
        assertEquals(2, loadedManager.getAllSubtasks().size(), "Должно быть загружено 2 подзадачи");

        Epic loadedEpic = loadedManager.getEpicById(epic.getId());
        assertEquals(epic.getName(), loadedEpic.getName());
        assertEquals(Status.IN_PROGRESS, loadedEpic.getStatus(), "Статус эпика должен быть IN_PROGRESS");
        assertEquals(2, loadedEpic.getSubtaskIds().size(), "У эпика должно быть 2 подзадачи");

        assertTrue(loadedEpic.getSubtaskIds().contains(subtask1.getId()));
        assertTrue(loadedEpic.getSubtaskIds().contains(subtask2.getId()));
    }

    @Test
    void saveAndLoadMixedData() {
        Task task = new Task("Простая задача", "Описание");
        Epic epic = new Epic("Эпик", "Описание эпика");

        manager.addTask(task);
        manager.addEpic(epic);

        Subtask subtask1 = new Subtask("Подзадача 1", "Описание", Status.NEW, epic.getId());
        Subtask subtask2 = new Subtask("Подзадача 2", "Описание", Status.DONE, epic.getId());

        manager.addSubtask(subtask1);
        manager.addSubtask(subtask2);

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(file);

        assertEquals(1, loadedManager.getAllTasks().size());
        assertEquals(1, loadedManager.getAllEpics().size());
        assertEquals(2, loadedManager.getAllSubtasks().size());

        // Проверяем, что новые ID генерируются правильно
        Task newTask = new Task("Новая задача", "Новое описание");
        loadedManager.addTask(newTask);

        assertTrue(newTask.getId() > Math.max(Math.max(task.getId(), epic.getId()), Math.max(subtask1.getId(), subtask2.getId())), "Новый ID должен быть больше всех существующих");
    }

    @Test
    void updateTaskAfterLoad() {
        Task task = new Task("Исходная задача", "Исходное описание");
        manager.addTask(task);

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(file);

        Task loadedTask = loadedManager.getTaskById(task.getId());
        loadedTask.setName("Обновленная задача");
        loadedTask.setStatus(Status.DONE);
        loadedManager.updateTask(loadedTask);

        FileBackedTaskManager reloadedManager = FileBackedTaskManager.loadFromFile(file);
        Task reloadedTask = reloadedManager.getTaskById(task.getId());

        assertEquals("Обновленная задача", reloadedTask.getName());
        assertEquals(Status.DONE, reloadedTask.getStatus());
    }

    @Test
    void deleteTaskAfterLoad() {
        Task task1 = new Task("Задача 1", "Описание 1");
        Task task2 = new Task("Задача 2", "Описание 2");

        manager.addTask(task1);
        manager.addTask(task2);

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(file);
        assertEquals(2, loadedManager.getAllTasks().size());

        loadedManager.deleteTaskById(task1.getId());

        FileBackedTaskManager reloadedManager = FileBackedTaskManager.loadFromFile(file);
        assertEquals(1, reloadedManager.getAllTasks().size());
        assertNull(reloadedManager.getTaskById(task1.getId()));
        assertNotNull(reloadedManager.getTaskById(task2.getId()));
    }

    @Test
    void deleteEpicWithSubtasksAfterLoad() {
        Epic epic = new Epic("Эпик", "Описание");
        manager.addEpic(epic);

        Subtask subtask1 = new Subtask("Подзадача 1", "Описание", Status.NEW, epic.getId());
        Subtask subtask2 = new Subtask("Подзадача 2", "Описание", Status.NEW, epic.getId());

        manager.addSubtask(subtask1);
        manager.addSubtask(subtask2);

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(file);
        assertEquals(1, loadedManager.getAllEpics().size());
        assertEquals(2, loadedManager.getAllSubtasks().size());

        loadedManager.deleteEpicById(epic.getId());

        FileBackedTaskManager reloadedManager = FileBackedTaskManager.loadFromFile(file);
        assertTrue(reloadedManager.getAllEpics().isEmpty());
        assertTrue(reloadedManager.getAllSubtasks().isEmpty());
    }

    @Test
    void clearTasksAfterLoad() {
        manager.addTask(new Task("Задача 1", "Описание"));
        manager.addTask(new Task("Задача 2", "Описание"));

        Epic epic = new Epic("Эпик", "Описание");
        manager.addEpic(epic);

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(file);
        assertEquals(2, loadedManager.getAllTasks().size());
        assertEquals(1, loadedManager.getAllEpics().size());

        loadedManager.clearTasks();

        FileBackedTaskManager reloadedManager = FileBackedTaskManager.loadFromFile(file);
        assertTrue(reloadedManager.getAllTasks().isEmpty());
        assertEquals(1, reloadedManager.getAllEpics().size());
    }

    @Test
    void saveExceptionHandling() {
        file.setReadOnly();

        assertThrows(ManagerSaveException.class, () -> {
            manager.addTask(new Task("Тест", "Описание"));
        }, "Должно выбрасываться исключение ManagerSaveException при ошибке сохранения");
    }

    @Test
    void loadFromNonExistentFile() throws IOException {
        File nonExistentFile = new File(tempDir.toFile(), "non_existent.csv");

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(nonExistentFile);

        assertTrue(loadedManager.getAllTasks().isEmpty());
        assertTrue(loadedManager.getAllEpics().isEmpty());
        assertTrue(loadedManager.getAllSubtasks().isEmpty());

        Task task = new Task("Новая задача", "Описание");
        loadedManager.addTask(task);
        assertEquals(1, task.getId());
    }

    @Test
    void csvFormatCorrectness() throws IOException {
        Task task = new Task("Задача", "Описание задачи");
        Epic epic = new Epic("Эпик", "Описание эпика");

        manager.addTask(task);
        manager.addEpic(epic);

        Subtask subtask = new Subtask("Подзадача", "Описание подзадачи", Status.IN_PROGRESS, epic.getId());
        manager.addSubtask(subtask);

        String content = Files.readString(file.toPath());
        String[] lines = content.split("\n");

        assertEquals("id,type,name,status,description,epic", lines[0]);

        assertEquals(4, lines.length);

        assertTrue(lines[1].contains("TASK"));
        assertTrue(lines[2].contains("EPIC"));
        assertTrue(lines[3].contains("SUBTASK"));
        assertTrue(lines[3].endsWith(String.valueOf(epic.getId())));
    }

    @Test
    void epicStatusRecalculationAfterLoad() {
        Epic epic = new Epic("Эпик", "Описание");
        manager.addEpic(epic);

        Subtask subtask1 = new Subtask("Подзадача 1", "Описание", Status.NEW, epic.getId());
        Subtask subtask2 = new Subtask("Подзадача 2", "Описание", Status.DONE, epic.getId());

        manager.addSubtask(subtask1);
        manager.addSubtask(subtask2);

        assertEquals(Status.IN_PROGRESS, manager.getEpicById(epic.getId()).getStatus());

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(file);
        Epic loadedEpic = loadedManager.getEpicById(epic.getId());
        assertEquals(Status.IN_PROGRESS, loadedEpic.getStatus());

        Subtask loadedSubtask1 = loadedManager.getSubtaskById(subtask1.getId());
        loadedSubtask1.setStatus(Status.DONE);
        loadedManager.updateSubtask(loadedSubtask1);

        assertEquals(Status.DONE, loadedManager.getEpicById(epic.getId()).getStatus());

        FileBackedTaskManager reloadedManager = FileBackedTaskManager.loadFromFile(file);
        assertEquals(Status.DONE, reloadedManager.getEpicById(epic.getId()).getStatus());
    }
}