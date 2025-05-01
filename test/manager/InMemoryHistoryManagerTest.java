package manager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tasks.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryHistoryManagerTest {
    private HistoryManager historyManager;
    private Task task;

    @BeforeEach
    void setUp() {
        historyManager = Managers.getDefaultHistory();
        task = new Task("Тестовая задача", "Описание тестовой задачи");
        task.setId(1);
    }

    @Test
    void add() {
        historyManager.add(task);

        List<Task> history = historyManager.getHistory();

        assertNotNull(history, "История не должна быть null");
        assertEquals(1, history.size(), "Размер истории должен быть 1");
        assertEquals(task, history.get(0), "Задача в истории должна соответствовать добавленной задаче");
    }

    @Test
    void addNullTask() {
        historyManager.add(null);

        List<Task> history = historyManager.getHistory();

        assertTrue(history.isEmpty(), "История должна быть пустой при добавлении null");
    }

    @Test
    void historyLimitedToTenItems() {
        // Создаем и добавляем 11 задач
        for (int i = 0; i < 11; i++) {
            Task t = new Task("Задача " + i, "Описание " + i);
            t.setId(i);
            historyManager.add(t);
        }

        List<Task> history = historyManager.getHistory();

        assertEquals(10, history.size(), "История должна содержать не более 10 элементов");
        assertEquals(1, history.get(0).getId(), "Самая старая задача должна быть удалена");
        assertEquals(10, history.get(9).getId(), "Последняя добавленная задача должна быть в истории");
    }

    @Test
    void getHistoryReturnsCopy() {
        historyManager.add(task);

        List<Task> history1 = historyManager.getHistory();
        List<Task> history2 = historyManager.getHistory();

        assertNotSame(history1, history2, "Метод getHistory должен возвращать копию списка истории");
    }
}