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

        assertNotNull(history, "История не должна быть null");
        assertEquals(List.of(task1), history, "История должна содержать одну задачу");
    }

    @Test
    void addNullTask() {
        historyManager.add(null);

        List<Task> history = historyManager.getHistory();

        assertTrue(history.isEmpty(), "История должна быть пустой при добавлении null");
    }

    @Test
    void addMultipleTasks() {
        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);

        List<Task> history = historyManager.getHistory();

        assertEquals(List.of(task1, task2, task3), history, "История должна содержать задачи в порядке добавления");
    }

    @Test
    void removeDuplicatesFromHistory() {
        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);

        historyManager.add(task1);

        List<Task> history = historyManager.getHistory();

        assertEquals(List.of(task2, task3, task1), history,
                "task1 должна переместиться в конец после повторного добавления");
    }

    @Test
    void removeTaskFromHistory() {
        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);

        historyManager.remove(task2.getId());

        List<Task> history = historyManager.getHistory();

        assertEquals(List.of(task1, task3), history, "История должна содержать task1 и task3 после удаления task2");
    }

    @Test
    void removeFirstTaskFromHistory() {
        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);

        historyManager.remove(task1.getId());

        List<Task> history = historyManager.getHistory();

        assertEquals(List.of(task2, task3), history, "История должна содержать task2 и task3 после удаления первой задачи");
    }

    @Test
    void removeLastTaskFromHistory() {
        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);

        historyManager.remove(task3.getId());

        List<Task> history = historyManager.getHistory();

        assertEquals(List.of(task1, task2), history, "История должна содержать task1 и task2 после удаления последней задачи");
    }

    @Test
    void removeNonExistentTask() {
        historyManager.add(task1);
        historyManager.add(task2);

        historyManager.remove(999); // Несуществующий ID

        List<Task> history = historyManager.getHistory();

        assertEquals(List.of(task1, task2), history, "История не должна измениться при удалении несуществующей задачи");
    }

    @Test
    void removeFromEmptyHistory() {
        historyManager.remove(1);

        List<Task> history = historyManager.getHistory();

        assertTrue(history.isEmpty(), "История должна остаться пустой");
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

        assertTrue(history.isEmpty(), "История должна быть пустой после удаления всех задач");
    }

    @Test
    void getHistoryReturnsCopy() {
        historyManager.add(task1);

        List<Task> history1 = historyManager.getHistory();
        List<Task> history2 = historyManager.getHistory();

        assertNotSame(history1, history2, "Метод getHistory должен возвращать копию списка истории");
    }

    @Test
    void historyIsUnlimited() {
        for (int i = 1; i <= 15; i++) {
            Task task = new Task("Задача " + i, "Описание " + i);
            task.setId(i);
            historyManager.add(task);
        }

        List<Task> history = historyManager.getHistory();

        assertEquals(15, history.size(), "История должна содержать все 15 задач");
        assertEquals(1, history.get(0).getId(), "Первая задача должна иметь ID = 1");
        assertEquals(15, history.get(14).getId(), "Последняя задача должна иметь ID = 15");
    }

    @Test
    void complexScenarioWithDuplicatesAndRemovals() {
        historyManager.add(task1); // [1]
        historyManager.add(task2); // [1, 2]
        historyManager.add(task3); // [1, 2, 3]
        historyManager.add(task1); // [2, 3, 1] - task1 переместилась в конец

        assertEquals(List.of(task2, task3, task1), historyManager.getHistory(),
                "После добавления дубликата порядок должен быть [2, 3, 1]");

        historyManager.remove(task3.getId()); // [2, 1]

        assertEquals(List.of(task2, task1), historyManager.getHistory(),
                "После удаления task3 должны остаться [2, 1]");

        historyManager.add(task3); // [2, 1, 3]

        assertEquals(List.of(task2, task1, task3), historyManager.getHistory(),
                "После повторного добавления task3 порядок должен быть [2, 1, 3]");
    }
}