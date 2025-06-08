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

            lines.add("id,type,name,status,description,epic");

            for (Task task : getAllTasks()) {
                lines.add(toString(task));
            }

            for (Epic epic : getAllEpics()) {
                lines.add(toString(epic));
            }

            for (Subtask subtask : getAllSubtasks()) {
                lines.add(toString(subtask));
            }

            Files.write(file.toPath(), lines);
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка сохранения в файл: " + file.getAbsolutePath(), e);
        }
    }

    private String toString(Task task) {
        TaskType type = getTaskType(task);
        String epicId = "";

        if (task instanceof Subtask) {
            epicId = String.valueOf(((Subtask) task).getEpicId());
        }

        return String.join(",",
                String.valueOf(task.getId()),
                type.toString(),
                task.getName(),
                task.getStatus().toString(),
                task.getDescription(),
                epicId
        );
    }

    private TaskType getTaskType(Task task) {
        if (task instanceof Epic) {
            return TaskType.EPIC;
        } else if (task instanceof Subtask) {
            return TaskType.SUBTASK;
        } else {
            return TaskType.TASK;
        }
    }

    private Task fromString(String value) {
        String[] parts = value.split(",");

        int id = Integer.parseInt(parts[0]);
        TaskType type = TaskType.valueOf(parts[1]);
        String name = parts[2];
        Status status = Status.valueOf(parts[3]);
        String description = parts[4];

        Task task;

        switch (type) {
            case TASK:
                task = new Task(name, description);
                break;
            case EPIC:
                task = new Epic(name, description);
                break;
            case SUBTASK:
                int epicId = Integer.parseInt(parts[5]);
                task = new Subtask(name, description, status, epicId);
                break;
            default:
                throw new IllegalArgumentException("Неизвестный тип задачи: " + type);
        }

        task.setId(id);
        task.setStatus(status);

        return task;
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

                Task task = manager.fromString(line);

                if (task instanceof Epic) {
                    manager.addEpicWithoutSave((Epic) task);
                } else if (task instanceof Subtask) {
                    manager.addSubtaskWithoutSave((Subtask) task);
                } else {
                    manager.addTaskWithoutSave(task);
                }
            }

            manager.updateNextId();

        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка загрузки из файла: " + file.getAbsolutePath(), e);
        }

        return manager;
    }

    private void addTaskWithoutSave(Task task) {
        int originalId = task.getId();
        tasks.put(originalId, task);
    }

    private void addEpicWithoutSave(Epic epic) {
        int originalId = epic.getId();
        epics.put(originalId, epic);
    }

    private void addSubtaskWithoutSave(Subtask subtask) {
        int originalId = subtask.getId();
        subtasks.put(originalId, subtask);

        Epic epic = epics.get(subtask.getEpicId());
        if (epic != null) {
            epic.getSubtaskIds().add(subtask.getId());
        }
    }

    private void updateNextId() {
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
        File file = new File("tasks.csv");

        System.out.println("=== Создание FileBackedTaskManager и добавление задач ===");

        FileBackedTaskManager manager = new FileBackedTaskManager(file);

        Task task1 = new Task("Покупка продуктов", "Купить молоко, хлеб, яйца");
        Task task2 = new Task("Уборка дома", "Пропылесосить и помыть полы");

        Epic epic1 = new Epic("Переезд", "Организация переезда в новую квартиру");

        manager.addTask(task1);
        manager.addTask(task2);
        manager.addEpic(epic1);

        Subtask subtask1 = new Subtask("Упаковать вещи", "Собрать все в коробки", Status.NEW, epic1.getId());
        Subtask subtask2 = new Subtask("Заказать грузчиков", "Найти компанию по переезду", Status.DONE, epic1.getId());

        manager.addSubtask(subtask1);
        manager.addSubtask(subtask2);

        System.out.println("Добавлено задач: " + manager.getAllTasks().size());
        System.out.println("Добавлено эпиков: " + manager.getAllEpics().size());
        System.out.println("Добавлено подзадач: " + manager.getAllSubtasks().size());

        System.out.println("\n=== Загрузка из того же файла ===");

        FileBackedTaskManager loadedManager = loadFromFile(file);

        System.out.println("Загружено задач: " + loadedManager.getAllTasks().size());
        System.out.println("Загружено эпиков: " + loadedManager.getAllEpics().size());
        System.out.println("Загружено подзадач: " + loadedManager.getAllSubtasks().size());

        System.out.println("\n=== Сравнение данных ===");

        System.out.println("Задачи совпадают: " +
                manager.getAllTasks().equals(loadedManager.getAllTasks()));
        System.out.println("Эпики совпадают: " +
                manager.getAllEpics().equals(loadedManager.getAllEpics()));
        System.out.println("Подзадачи совпадают: " +
                manager.getAllSubtasks().equals(loadedManager.getAllSubtasks()));

        file.delete();
    }
}