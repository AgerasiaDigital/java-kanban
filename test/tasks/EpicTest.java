package tasks;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class EpicTest {

    @Test
    void epicEquality() {
        Epic epic1 = new Epic("Эпик", "Описание эпика");
        epic1.setId(1);

        Epic epic2 = new Epic("Другой эпик", "Другое описание эпика");
        epic2.setId(1);

        Epic epic3 = new Epic("Эпик", "Описание эпика");
        epic3.setId(2);

        assertEquals(epic1, epic2, "Эпики с одинаковым ID должны быть равны");
        assertEquals(epic1.hashCode(), epic2.hashCode(), "Хеш-коды эпиков с одинаковым ID должны быть равны");

        assertNotEquals(epic1, epic3, "Эпики с разным ID не должны быть равны");
        assertNotEquals(epic1.hashCode(), epic3.hashCode(), "Хеш-коды эпиков с разным ID не должны быть равны");
    }

    @Test
    void epicTaskEquality() {
        Epic epic = new Epic("Эпик", "Описание эпика");
        epic.setId(1);

        Task task = new Task("Задача", "Описание задачи");
        task.setId(1);

        assertEquals(epic, task, "Эпик и задача с одинаковым ID должны быть равны");
        assertEquals(epic.hashCode(), task.hashCode(), "Хеш-коды эпика и задачи с одинаковым ID должны быть равны");
    }
}