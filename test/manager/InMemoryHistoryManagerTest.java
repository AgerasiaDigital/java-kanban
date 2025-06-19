package manager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tasks.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryHistoryManagerTest {
    private HistoryManager historyManager;
    private Task task1;
    private Task task2;
    private Task task3;

    @BeforeEach
    void setUp() {
        historyManager = Managers.getDefaultHistory();
        task1 = new Task("Задача 1", "Описание 1");
        task1.setId(1);
        task2 = new Task("Задача 2", "Описание 2");
        task2.setId(2);
        task3 = new Task("Задача 3", "Описание 3");
        task3.setId(3);
    }

    @Test
    void addSingleTask() {
        historyManager.add(task1);

        List<Task> history = historyManager.getHistory();

        assertNotNull(history);
        assertEquals(List.of(task1), history);
    }

    @Test
    void addNullTask() {
        historyManager.add(null);

        List<Task> history = historyManager.getHistory();

        assertTrue(history.isEmpty());
    }

    @Test
    void addMultipleTasks() {
        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);

        List<Task> history = historyManager.getHistory();

        assertEquals(List.of(task1, task2, task3), history);
    }

    @Test
    void removeDuplicatesFromHistory() {
        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);

        historyManager.add(task1);

        List<Task> history = historyManager.getHistory();

        assertEquals(List.of(task2, task3, task1), history);
    }

    @Test
    void removeTaskFromHistory() {
        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);

        historyManager.remove(task2.getId());

        List<Task> history = historyManager.getHistory();

        assertEquals(List.of(task1, task3), history);
    }

    @Test
    void removeFirstTaskFromHistory() {
        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);

        historyManager.remove(task1.getId());

        List<Task> history = historyManager.getHistory();

        assertEquals(List.of(task2, task3), history);
    }

    @Test
    void removeLastTaskFromHistory() {
        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);

        historyManager.remove(task3.getId());

        List<Task> history = historyManager.getHistory();

        assertEquals(List.of(task1, task2), history);
    }

    @Test
    void removeNonExistentTask() {
        historyManager.add(task1);
        historyManager.add(task2);

        historyManager.remove(999);

        List<Task> history = historyManager.getHistory();

        assertEquals(List.of(task1, task2), history);
    }

    @Test
    void removeFromEmptyHistory() {
        historyManager.remove(1);

        List<Task> history = historyManager.getHistory();

        assertTrue(history.isEmpty());
    }

    @Test
    void removeAllTasksFromHistory() {
        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);

        historyManager.remove(task1.getId());
        historyManager.remove(task2.getId());
        historyManager.remove(task3.getId());

        List<Task> history = historyManager.getHistory();

        assertTrue(history.isEmpty());
    }

    @Test
    void getHistoryReturnsCopy() {
        historyManager.add(task1);

        List<Task> history1 = historyManager.getHistory();
        List<Task> history2 = historyManager.getHistory();

        assertNotSame(history1, history2);
    }

    @Test
    void emptyHistoryTest() {
        List<Task> history = historyManager.getHistory();

        assertTrue(history.isEmpty());
    }

    @Test
    void historyIsUnlimited() {
        for (int i = 1; i <= 15; i++) {
            Task task = new Task("Задача " + i, "Описание " + i);
            task.setId(i);
            historyManager.add(task);
        }

        List<Task> history = historyManager.getHistory();

        assertEquals(15, history.size());
        assertEquals(1, history.get(0).getId());
        assertEquals(15, history.get(14).getId());
    }

    @Test
    void complexScenarioWithDuplicatesAndRemovals() {
        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);
        historyManager.add(task1);

        assertEquals(List.of(task2, task3, task1), historyManager.getHistory());

        historyManager.remove(task3.getId());

        assertEquals(List.of(task2, task1), historyManager.getHistory());

        historyManager.add(task3);

        assertEquals(List.of(task2, task1, task3), historyManager.getHistory());
    }
}