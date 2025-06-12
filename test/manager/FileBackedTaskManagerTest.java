package manager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import tasks.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class FileBackedTaskManagerTest {

    @TempDir
    Path tempDir;

    private File file;
    private FileBackedTaskManager manager;

    private Task task;
    private Epic epic;
    private Subtask subtask;

    @BeforeEach
    void setUp() throws IOException {
        file = Files.createTempFile(tempDir, "test", ".csv").toFile();
        manager = new FileBackedTaskManager(file);

        task = new Task("Тестовая задача", "Описание задачи");
        epic = new Epic("Тестовый эпик", "Описание эпика");
        subtask = new Subtask("Тестовая подзадача", "Описание подзадачи", Status.IN_PROGRESS, 0);
    }

    @AfterEach
    void tearDown() {
        if (file.exists()) {
            file.delete();
        }
    }

    @Test
    void saveAndLoadEmptyManager() {
        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(file);

        assertTrue(loadedManager.getAllTasks().isEmpty(), "Список задач должен быть пустым");
        assertTrue(loadedManager.getAllEpics().isEmpty(), "Список эпиков должен быть пустым");
        assertTrue(loadedManager.getAllSubtasks().isEmpty(), "Список подзадач должен быть пустым");
    }

    @Test
    void saveAndLoadFilledManager() {
        manager.addTask(task);
        manager.addEpic(epic);

        subtask.setEpicId(epic.getId());
        manager.addSubtask(subtask);

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(file);

        assertEquals(manager.getAllTasks().size(), loadedManager.getAllTasks().size(), "Количество задач должно совпадать");
        assertEquals(manager.getAllEpics().size(), loadedManager.getAllEpics().size(), "Количество эпиков должно совпадать");
        assertEquals(manager.getAllSubtasks().size(), loadedManager.getAllSubtasks().size(), "Количество подзадач должно совпадать");

        Task loadedTask = loadedManager.getTaskById(task.getId());
        Epic loadedEpic = loadedManager.getEpicById(epic.getId());
        Subtask loadedSubtask = loadedManager.getSubtaskById(subtask.getId());

        assertNotNull(loadedTask, "Задача должна быть загружена");
        assertNotNull(loadedEpic, "Эпик должен быть загружен");
        assertNotNull(loadedSubtask, "Подзадача должна быть загружена");

        assertEquals(task.getName(), loadedTask.getName(), "Название задачи должно совпадать");
        assertEquals(task.getDescription(), loadedTask.getDescription(), "Описание задачи должно совпадать");
        assertEquals(task.getStatus(), loadedTask.getStatus(), "Статус задачи должен совпадать");

        assertEquals(epic.getName(), loadedEpic.getName(), "Название эпика должно совпадать");
        assertEquals(subtask.getEpicId(), loadedSubtask.getEpicId(), "ID эпика у подзадачи должен совпадать");

        assertTrue(loadedEpic.getSubtaskIds().contains(loadedSubtask.getId()), "Подзадача должна быть связана с эпиком");
    }

    @Test
    void loadFromNonExistentFile() {
        File nonExistentFile = new File(tempDir.toFile(), "non_existent.csv");

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(nonExistentFile);

        assertTrue(loadedManager.getAllTasks().isEmpty(), "Список задач должен быть пустым");
        assertTrue(loadedManager.getAllEpics().isEmpty(), "Список эпиков должен быть пустым");
        assertTrue(loadedManager.getAllSubtasks().isEmpty(), "Список подзадач должен быть пустым");

        Task newTask = new Task("Новая задача", "Описание");
        loadedManager.addTask(newTask);
        assertEquals(1, newTask.getId(), "ID должен генерироваться правильно");
    }

    @Test
    void saveExceptionHandling() {
        file.setReadOnly();

        assertThrows(ManagerSaveException.class, () -> {
            manager.addTask(new Task("Тест", "Описание"));
        }, "Должно выбрасываться исключение ManagerSaveException при ошибке сохранения");
    }

    @Test
    void updateTaskInFile() {
        manager.addTask(task);

        task.setName("Обновленная задача");
        task.setStatus(Status.DONE);
        manager.updateTask(task);

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(file);
        Task loadedTask = loadedManager.getTaskById(task.getId());

        assertEquals("Обновленная задача", loadedTask.getName(), "Название должно обновиться в файле");
        assertEquals(Status.DONE, loadedTask.getStatus(), "Статус должен обновиться в файле");
    }

    @Test
    void deleteTaskFromFile() {

        manager.addTask(task);
        Epic epic2 = new Epic("Эпик 2", "Описание 2");
        manager.addEpic(epic2);

        manager.deleteTaskById(task.getId());

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(file);

        assertNull(loadedManager.getTaskById(task.getId()), "Задача должна быть удалена из файла");
        assertNotNull(loadedManager.getEpicById(epic2.getId()), "Эпик должен остаться в файле");
        assertEquals(0, loadedManager.getAllTasks().size(), "Список задач должен быть пустым");
        assertEquals(1, loadedManager.getAllEpics().size(), "Должен остаться один эпик");
    }
}