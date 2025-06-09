package manager;

import tasks.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class FileBackedTaskManager extends InMemoryTaskManager {
    private final File file;

    public FileBackedTaskManager(File file) {
        this.file = file;
    }

    @Override
    public void addTask(Task task) {
        super.addTask(task);
        save();
    }

    @Override
    public void addEpic(Epic epic) {
        super.addEpic(epic);
        save();
    }

    @Override
    public void addSubtask(Subtask subtask) {
        super.addSubtask(subtask);
        save();
    }

    @Override
    public void updateTask(Task task) {
        super.updateTask(task);
        save();
    }

    @Override
    public void updateEpic(Epic epic) {
        super.updateEpic(epic);
        save();
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        super.updateSubtask(subtask);
        save();
    }

    @Override
    public void deleteTaskById(int id) {
        super.deleteTaskById(id);
        save();
    }

    @Override
    public void deleteEpicById(int id) {
        super.deleteEpicById(id);
        save();
    }

    @Override
    public void deleteSubtaskById(int id) {
        super.deleteSubtaskById(id);
        save();
    }

    @Override
    public void clearTasks() {
        super.clearTasks();
        save();
    }

    @Override
    public void clearEpics() {
        super.clearEpics();
        save();
    }

    @Override
    public void clearSubtasks() {
        super.clearSubtasks();
        save();
    }

    private void save() {
        try {
            List<String> lines = new ArrayList<>();

            lines.add(CSVTaskFormatter.getHeader());

            // Используем утилитарный класс вместо собственных методов
            for (Task task : getAllTasks()) {
                lines.add(CSVTaskFormatter.toString(task));
            }

            for (Epic epic : getAllEpics()) {
                lines.add(CSVTaskFormatter.toString(epic));
            }

            for (Subtask subtask : getAllSubtasks()) {
                lines.add(CSVTaskFormatter.toString(subtask));
            }

            Files.write(file.toPath(), lines);
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка сохранения в файл: " + file.getAbsolutePath(), e);
        }
    }

    public static FileBackedTaskManager loadFromFile(File file) {
        FileBackedTaskManager manager = new FileBackedTaskManager(file);

        if (!file.exists()) {
            return manager;
        }

        try {
            String content = Files.readString(file.toPath());
            String[] lines = content.split("\n");

            for (int i = 1; i < lines.length; i++) {
                String line = lines[i].trim();
                if (line.isEmpty()) {
                    continue;
                }

                Task task = CSVTaskFormatter.fromString(line);

                // Убираем instanceof, используем switch по типу
                switch (task.getType()) {
                    case TASK:
                        manager.tasks.put(task.getId(), task);
                        break;
                    case EPIC:
                        manager.epics.put(task.getId(), (Epic) task);
                        break;
                    case SUBTASK:
                        Subtask subtask = (Subtask) task;
                        manager.subtasks.put(task.getId(), subtask);

                        // Добавляем подзадачу в эпик
                        Epic epic = manager.epics.get(subtask.getEpicId());
                        if (epic != null) {
                            epic.getSubtaskIds().add(subtask.getId());
                        }
                        break;
                }
            }

            manager.updateAfterLoad();

        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка загрузки из файла: " + file.getAbsolutePath(), e);
        }

        return manager;
    }

    private void updateAfterLoad() {
        int maxId = 0;

        for (Task task : getAllTasks()) {
            maxId = Math.max(maxId, task.getId());
        }
        for (Epic epic : getAllEpics()) {
            maxId = Math.max(maxId, epic.getId());
            updateEpicStatus(epic);
        }
        for (Subtask subtask : getAllSubtasks()) {
            maxId = Math.max(maxId, subtask.getId());
        }

        this.nextId = maxId + 1;
    }

    public static void main(String[] args) {
        File file = new File("demo_tasks.csv");

        System.out.println("=== Создание FileBackedTaskManager и добавление задач ===");

        FileBackedTaskManager manager = new FileBackedTaskManager(file);

        Task task1 = new Task("Покупка продуктов", "Купить молоко, хлеб, яйца");
        Epic epic1 = new Epic("Переезд", "Организация переезда в новую квартиру");

        manager.addTask(task1);
        manager.addEpic(epic1);

        Subtask subtask1 = new Subtask("Упаковать вещи", "Собрать все в коробки", Status.NEW, epic1.getId());
        Subtask subtask2 = new Subtask("Заказать грузчиков", "Найти компанию по переезду", Status.DONE, epic1.getId());

        manager.addSubtask(subtask1);
        manager.addSubtask(subtask2);

        System.out.printf("Добавлено: %d задач, %d эпиков, %d подзадач%n",
                manager.getAllTasks().size(),
                manager.getAllEpics().size(),
                manager.getAllSubtasks().size());

        System.out.println("\n=== Загрузка из того же файла ===");

        FileBackedTaskManager loadedManager = loadFromFile(file);

        System.out.printf("Загружено: %d задач, %d эпиков, %d подзадач%n",
                loadedManager.getAllTasks().size(),
                loadedManager.getAllEpics().size(),
                loadedManager.getAllSubtasks().size());

        System.out.println("\n=== Сравнение данных ===");

        System.out.println("Задачи совпадают: " +
                manager.getAllTasks().equals(loadedManager.getAllTasks()));
        System.out.println("Эпики совпадают: " +
                manager.getAllEpics().equals(loadedManager.getAllEpics()));
        System.out.println("Подзадачи совпадают: " +
                manager.getAllSubtasks().equals(loadedManager.getAllSubtasks()));

        file.delete();

        System.out.println("\n=== ДЕМОНСТРАЦИЯ ЗАВЕРШЕНА ===");
    }
}