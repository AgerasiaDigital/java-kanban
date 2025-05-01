package manager;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ManagersTest {

    @Test
    void getDefault() {
        TaskManager taskManager = Managers.getDefault();

        assertNotNull(taskManager, "TaskManager не должен быть null");
        assertTrue(taskManager instanceof InMemoryTaskManager, "TaskManager должен быть экземпляром InMemoryTaskManager");
    }

    @Test
    void getDefaultHistory() {
        HistoryManager historyManager = Managers.getDefaultHistory();

        assertNotNull(historyManager, "HistoryManager не должен быть null");
        assertTrue(historyManager instanceof InMemoryHistoryManager, "HistoryManager должен быть экземпляром InMemoryHistoryManager");
    }
}