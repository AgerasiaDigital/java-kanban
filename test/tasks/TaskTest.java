package tasks;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TaskTest {

    @Test
    void taskEquality() {
        Task task1 = new Task("Задача", "Описание");
        task1.setId(1);

        Task task2 = new Task("Другая задача", "Другое описание");
        task2.setId(1);

        Task task3 = new Task("Задача", "Описание");
        task3.setId(2);

        assertEquals(task1, task2, "Задачи с одинаковым ID должны быть равны");
        assertEquals(task1.hashCode(), task2.hashCode(), "Хеш-коды задач с одинаковым ID должны быть равны");

        assertNotEquals(task1, task3, "Задачи с разным ID не должны быть равны");
        assertNotEquals(task1.hashCode(), task3.hashCode(), "Хеш-коды задач с разным ID не должны быть равны");
    }
}