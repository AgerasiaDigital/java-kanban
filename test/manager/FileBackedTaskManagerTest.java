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
import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class FileBackedTaskManagerTest extends TaskManagerTest<FileBackedTaskManager> {

    @TempDir
    Path tempDir;

    private File file;

    @BeforeEach
    void setUp() throws IOException {
        file = Files.createTempFile(tempDir, "test", ".csv").toFile();
        taskManager = createTaskManager();
    }

    @AfterEach
    void tearDown() {
        if (file.exists()) {
            file.delete();
        }
    }

    @Override
    protected FileBackedTaskManager createTaskManager() {
        return new FileBackedTaskManager(file);
    }

    @Test
    void saveAndLoadEmptyManager() {
        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(file);

        assertTrue(loadedManager.getAllTasks().isEmpty());
        assertTrue(loadedManager.getAllEpics().isEmpty());
        assertTrue(loadedManager.getAllSubtasks().isEmpty());
    }

    @Test
    void saveAndLoadFilledManager() {
        LocalDateTime now = LocalDateTime.now().withNano(0);

        Task task = new Task("Тестовая задача", "Описание задачи", Duration.ofHours(1), now);
        Epic epic = new Epic("Тестовый эпик", "Описание эпика");

        taskManager.addTask(task);
        taskManager.addEpic(epic);

        Subtask subtask = new Subtask("Тестовая подзадача", "Описание подзадачи",
                Status.IN_PROGRESS, epic.getId(), Duration.ofMinutes(30), now.plusHours(2));
        taskManager.addSubtask(subtask);

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(file);

        assertEquals(taskManager.getAllTasks().size(), loadedManager.getAllTasks().size());
        assertEquals(taskManager.getAllEpics().size(), loadedManager.getAllEpics().size());
        assertEquals(taskManager.getAllSubtasks().size(), loadedManager.getAllSubtasks().size());

        Task loadedTask = loadedManager.getTaskById(task.getId());
        Epic loadedEpic = loadedManager.getEpicById(epic.getId());
        Subtask loadedSubtask = loadedManager.getSubtaskById(subtask.getId());

        assertNotNull(loadedTask);
        assertNotNull(loadedEpic);
        assertNotNull(loadedSubtask);

        assertEquals(task.getName(), loadedTask.getName());
        assertEquals(task.getDuration(), loadedTask.getDuration());
        assertEquals(task.getStartTime(), loadedTask.getStartTime());

        assertEquals(epic.getName(), loadedEpic.getName());
        assertEquals(subtask.getEpicId(), loadedSubtask.getEpicId());

        assertTrue(loadedEpic.getSubtaskIds().contains(loadedSubtask.getId()));
    }

    @Test
    void loadFromNonExistentFile() {
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
    void saveExceptionHandling() {
        file.setReadOnly();

        assertThrows(ManagerSaveException.class, () -> {
            taskManager.addTask(new Task("Тест", "Описание"));
        });
    }
}