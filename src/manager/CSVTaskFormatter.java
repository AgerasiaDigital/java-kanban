package manager;

import tasks.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class CSVTaskFormatter {
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static String toString(Task task) {
        String epicId = "";
        String durationStr = "";
        String startTimeStr = "";

        if (task.getType() == TaskType.SUBTASK) {
            epicId = String.valueOf(((Subtask) task).getEpicId());
        }

        if (task.getDuration() != null) {
            durationStr = String.valueOf(task.getDuration().toMinutes());
        }

        if (task.getStartTime() != null) {
            startTimeStr = task.getStartTime().format(DATE_TIME_FORMATTER);
        }

        return String.join(",",
                String.valueOf(task.getId()),
                task.getType().toString(),
                task.getName(),
                task.getStatus().toString(),
                task.getDescription(),
                epicId,
                durationStr,
                startTimeStr
        );
    }

    public static Task fromString(String value) {
        String[] parts = value.split(",");

        int id = Integer.parseInt(parts[0]);
        TaskType type = TaskType.valueOf(parts[1]);
        String name = parts[2];
        Status status = Status.valueOf(parts[3]);
        String description = parts[4];

        Duration duration = null;
        LocalDateTime startTime = null;

        if (parts.length > 6 && !parts[6].isEmpty()) {
            duration = Duration.ofMinutes(Long.parseLong(parts[6]));
        }

        if (parts.length > 7 && !parts[7].isEmpty()) {
            startTime = LocalDateTime.parse(parts[7], DATE_TIME_FORMATTER);
        }

        Task task;

        switch (type) {
            case TASK:
                task = new Task(name, description, duration, startTime);
                break;
            case EPIC:
                task = new Epic(name, description);
                break;
            case SUBTASK:
                int epicId = Integer.parseInt(parts[5]);
                task = new Subtask(name, description, status, epicId, duration, startTime);
                break;
            default:
                throw new IllegalArgumentException("Неизвестный тип задачи: " + type);
        }

        task.setId(id);
        task.setStatus(status);

        return task;
    }

    public static String getHeader() {
        return "id,type,name,status,description,epic,duration,startTime";
    }
}