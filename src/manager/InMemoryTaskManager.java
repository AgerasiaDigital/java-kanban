package manager;

import tasks.*;

import java.util.*;
import java.util.stream.Collectors;

public class InMemoryTaskManager implements TaskManager {
    protected int nextId = 1;
    protected final HashMap<Integer, Task> tasks = new HashMap<>();
    protected final HashMap<Integer, Epic> epics = new HashMap<>();
    protected final HashMap<Integer, Subtask> subtasks = new HashMap<>();
    protected final TreeSet<Task> prioritizedTasks = new TreeSet<>(
            Comparator.comparing(Task::getStartTime, Comparator.nullsLast(Comparator.naturalOrder()))
                    .thenComparing(Task::getId));
    private final HistoryManager historyManager = Managers.getDefaultHistory();

    @Override
    public void addTask(Task task) {
        if (checkTimeConflict(task)) {
            throw new IllegalArgumentException("Задача пересекается с существующими по времени");
        }
        task.setId(nextId++);
        tasks.put(task.getId(), task);
        prioritizedTasks.add(task);
    }

    @Override
    public void addEpic(Epic epic) {
        epic.setId(nextId++);
        epics.put(epic.getId(), epic);
    }

    @Override
    public void addSubtask(Subtask subtask) {
        Epic epic = epics.get(subtask.getEpicId());
        if (epic == null) return;

        if (checkTimeConflict(subtask)) {
            throw new IllegalArgumentException("Подзадача пересекается с существующими по времени");
        }

        subtask.setId(nextId++);
        subtasks.put(subtask.getId(), subtask);
        epic.getSubtaskIds().add(subtask.getId());

        prioritizedTasks.add(subtask);

        updateEpicStatus(epic);
        updateEpicTime(epic);
    }

    @Override
    public List<Task> getAllTasks() {
        return new ArrayList<>(tasks.values());
    }

    @Override
    public List<Epic> getAllEpics() {
        return new ArrayList<>(epics.values());
    }

    @Override
    public List<Subtask> getAllSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    @Override
    public List<Subtask> getSubtasksByEpicId(int epicId) {
        return subtasks.values().stream()
                .filter(subtask -> subtask.getEpicId() == epicId)
                .collect(Collectors.toList());
    }

    @Override
    public Task getTaskById(int id) {
        Task task = tasks.get(id);
        historyManager.add(task);
        return task;
    }

    @Override
    public Epic getEpicById(int id) {
        Epic epic = epics.get(id);
        historyManager.add(epic);
        return epic;
    }

    @Override
    public Subtask getSubtaskById(int id) {
        Subtask subtask = subtasks.get(id);
        historyManager.add(subtask);
        return subtask;
    }

    @Override
    public void updateTask(Task task) {
        if (tasks.containsKey(task.getId())) {
            Task oldTask = tasks.get(task.getId());
            prioritizedTasks.remove(oldTask);

            if (checkTimeConflict(task)) {
                if (oldTask.getStartTime() != null) {
                    prioritizedTasks.add(oldTask);
                }
                throw new IllegalArgumentException("Задача пересекается с существующими по времени");
            }

            tasks.put(task.getId(), task);
            prioritizedTasks.add(task);
        }
    }

    @Override
    public void updateEpic(Epic epic) {
        if (epics.containsKey(epic.getId())) {
            Epic oldEpic = epics.get(epic.getId());
            epic.getSubtaskIds().addAll(oldEpic.getSubtaskIds());
            epics.put(epic.getId(), epic);
            updateEpicStatus(epic);
            updateEpicTime(epic);
        }
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        if (subtasks.containsKey(subtask.getId())) {
            Subtask oldSubtask = subtasks.get(subtask.getId());
            prioritizedTasks.remove(oldSubtask);

            if (checkTimeConflict(subtask)) {
                if (oldSubtask.getStartTime() != null) {
                    prioritizedTasks.add(oldSubtask);
                }
                throw new IllegalArgumentException("Подзадача пересекается с существующими по времени");
            }

            subtasks.put(subtask.getId(), subtask);
            prioritizedTasks.add(subtask);

            Epic epic = epics.get(subtask.getEpicId());
            if (epic != null) {
                updateEpicStatus(epic);
                updateEpicTime(epic);
            }
        }
    }

    @Override
    public void deleteTaskById(int id) {
        Task task = tasks.remove(id);
        if (task != null) {
            prioritizedTasks.remove(task);
            historyManager.remove(id);
        }
    }

    @Override
    public void deleteEpicById(int id) {
        Epic epic = epics.remove(id);
        if (epic != null) {
            for (Integer subId : epic.getSubtaskIds()) {
                Subtask subtask = subtasks.remove(subId);
                if (subtask != null) {
                    prioritizedTasks.remove(subtask);
                }
                historyManager.remove(subId);
            }
            historyManager.remove(id);
        }
    }

    @Override
    public void deleteSubtaskById(int id) {
        Subtask subtask = subtasks.remove(id);
        if (subtask != null) {
            prioritizedTasks.remove(subtask);
            Epic epic = epics.get(subtask.getEpicId());
            if (epic != null) {
                epic.getSubtaskIds().remove((Integer) id);
                updateEpicStatus(epic);
                updateEpicTime(epic);
            }
            historyManager.remove(id);
        }
    }

    @Override
    public void clearTasks() {
        tasks.values().forEach(prioritizedTasks::remove);
        for (Integer taskId : tasks.keySet()) {
            historyManager.remove(taskId);
        }
        tasks.clear();
    }

    @Override
    public void clearEpics() {
        subtasks.values().forEach(prioritizedTasks::remove);
        for (Integer epicId : epics.keySet()) {
            historyManager.remove(epicId);
        }
        for (Integer subtaskId : subtasks.keySet()) {
            historyManager.remove(subtaskId);
        }
        epics.clear();
        subtasks.clear();
    }

    @Override
    public void clearSubtasks() {
        subtasks.values().forEach(prioritizedTasks::remove);
        for (Integer subtaskId : subtasks.keySet()) {
            historyManager.remove(subtaskId);
        }

        for (Epic epic : epics.values()) {
            epic.getSubtaskIds().clear();
            updateEpicStatus(epic);
            updateEpicTime(epic);
        }
        subtasks.clear();
    }

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    @Override
    public List<Task> getPrioritizedTasks() {
        return new ArrayList<>(prioritizedTasks);
    }

    protected void updateEpicStatus(Epic epic) {
        List<Integer> subIds = epic.getSubtaskIds();
        if (subIds.isEmpty()) {
            epic.setStatus(Status.NEW);
            return;
        }

        int newCount = 0;
        int doneCount = 0;
        for (Integer id : subIds) {
            Status status = subtasks.get(id).getStatus();
            if (status == Status.NEW) newCount++;
            else if (status == Status.DONE) doneCount++;
        }

        if (doneCount == subIds.size()) {
            epic.setStatus(Status.DONE);
        } else if (newCount == subIds.size()) {
            epic.setStatus(Status.NEW);
        } else {
            epic.setStatus(Status.IN_PROGRESS);
        }
    }

    private void updateEpicTime(Epic epic) {
        ArrayList<Subtask> epicSubtasks = epic.getSubtaskIds().stream()
                .map(subtasks::get)
                .collect(Collectors.toCollection(ArrayList::new));
        epic.calculateTimeFields(epicSubtasks);
    }

    private boolean checkTimeConflict(Task newTask) {
        if (newTask.getStartTime() == null || newTask.getDuration() == null) {
            return false;
        }

        List<Task> sortedTasks = getPrioritizedTasks();

        for (Task existingTask : sortedTasks) {
            if (existingTask.getId() == newTask.getId()) {
                continue;
            }

            if (existingTask.getStartTime().isAfter(newTask.getEndTime()) ||
                    existingTask.getStartTime().equals(newTask.getEndTime())) {
                break;
            }

            if (isTasksOverlap(newTask, existingTask)) {
                return true;
            }
        }

        return false;
    }

    private boolean isTasksOverlap(Task task1, Task task2) {
        if (task1.getStartTime() == null || task1.getDuration() == null ||
                task2.getStartTime() == null || task2.getDuration() == null) {
            return false;
        }

        return task1.getStartTime().isBefore(task2.getEndTime()) &&
                task2.getStartTime().isBefore(task1.getEndTime());
    }
}