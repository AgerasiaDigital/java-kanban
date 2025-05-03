package tasks;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SubtaskTest {

    @Test
    void subtaskEquality() {
        Subtask subtask1 = new Subtask("Подзадача", "Описание подзадачи", Status.NEW, 100);
        subtask1.setId(1);

        Subtask subtask2 = new Subtask("Другая подзадача", "Другое описание подзадачи", Status.IN_PROGRESS, 200);
        subtask2.setId(1);

        Subtask subtask3 = new Subtask("Подзадача", "Описание подзадачи", Status.NEW, 100);
        subtask3.setId(2);

        assertEquals(subtask1, subtask2, "Подзадачи с одинаковым ID должны быть равны");
        assertEquals(subtask1.hashCode(), subtask2.hashCode(), "Хеш-коды подзадач с одинаковым ID должны быть равны");

        assertNotEquals(subtask1, subtask3, "Подзадачи с разным ID не должны быть равны");
        assertNotEquals(subtask1.hashCode(), subtask3.hashCode(), "Хеш-коды подзадач с разным ID не должны быть равны");
    }

    @Test
    void subtaskTaskEquality() {
        Subtask subtask = new Subtask("Подзадача", "Описание подзадачи", Status.NEW, 100);
        subtask.setId(1);

        Task task = new Task("Задача", "Описание задачи");
        task.setId(1);

        assertNotEquals(subtask, task, "Подзадача и задача с одинаковым ID не должны быть равны");
    }

    @Test
    void subtaskEpicEquality() {
        Subtask subtask = new Subtask("Подзадача", "Описание подзадачи", Status.NEW, 100);
        subtask.setId(1);

        Epic epic = new Epic("Эпик", "Описание эпика");
        epic.setId(1);

        assertNotEquals(subtask, epic, "Подзадача и эпик с одинаковым ID не должны быть равны");
    }
}