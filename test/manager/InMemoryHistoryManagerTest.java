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
        assertEquals(1, history.size(), "Размер истории должен быть 1");
        assertEquals(task1, history.get(0), "Задача в истории должна соответствовать добавленной задаче");
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

        assertEquals(3, history.size(), "История должна содержать 3 элемента");
        assertEquals(task1, history.get(0), "Первая задача должна быть в начале истории");
        assertEquals(task2, history.get(1), "Вторая задача должна быть в середине истории");
        assertEquals(task3, history.get(2), "Третья задача должна быть в конце истории");
    }

    @Test
    void removeDuplicatesFromHistory() {
        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);

        historyManager.add(task1);

        List<Task> history = historyManager.getHistory();

        assertEquals(3, history.size(), "История должна содержать 3 уникальные задачи");
        assertEquals(task2, history.get(0), "task2 должна быть первой");
        assertEquals(task3, history.get(1), "task3 должна быть второй");
        assertEquals(task1, history.get(2), "task1 должна быть последней после повторного добавления");
    }

    @Test
    void removeTaskFromHistory() {
        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);

        historyManager.remove(task2.getId());

        List<Task> history = historyManager.getHistory();

        assertEquals(2, history.size(), "История должна содержать 2 элемента после удаления");
        assertEquals(task1, history.get(0), "task1 должна остаться первой");
        assertEquals(task3, history.get(1), "task3 должна остаться второй");
        assertFalse(history.contains(task2), "task2 не должна быть в истории");
    }

    @Test
    void removeFirstTaskFromHistory() {
        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);

        historyManager.remove(task1.getId());

        List<Task> history = historyManager.getHistory();

        assertEquals(2, history.size(), "История должна содержать 2 элемента");
        assertEquals(task2, history.get(0), "task2 должна стать первой");
        assertEquals(task3, history.get(1), "task3 должна остаться второй");
    }

    @Test
    void removeLastTaskFromHistory() {
        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);

        historyManager.remove(task3.getId());

        List<Task> history = historyManager.getHistory();

        assertEquals(2, history.size(), "История должна содержать 2 элемента");
        assertEquals(task1, history.get(0), "task1 должна остаться первой");
        assertEquals(task2, history.get(1), "task2 должна остаться второй");
    }

    @Test
    void removeNonExistentTask() {
        historyManager.add(task1);
        historyManager.add(task2);

        historyManager.remove(999);

        List<Task> history = historyManager.getHistory();

        assertEquals(2, history.size(), "Размер истории не должен измениться");
        assertEquals(task1, history.get(0), "task1 должна остаться на месте");
        assertEquals(task2, history.get(1), "task2 должна остаться на месте");
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
        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);
        historyManager.add(task1);

        List<Task> history = historyManager.getHistory();
        assertEquals(3, history.size());
        assertEquals(task2, history.get(0));
        assertEquals(task3, history.get(1));
        assertEquals(task1, history.get(2));

        historyManager.remove(task3.getId()); // [2, 1]

        history = historyManager.getHistory();
        assertEquals(2, history.size());
        assertEquals(task2, history.get(0));
        assertEquals(task1, history.get(1));

        historyManager.add(task3); // [2, 1, 3]

        history = historyManager.getHistory();
        assertEquals(3, history.size());
        assertEquals(task2, history.get(0));
        assertEquals(task1, history.get(1));
        assertEquals(task3, history.get(2));
    }
}