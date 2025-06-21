package tasks;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;

public class Epic extends Task {
    private ArrayList<Integer> subtaskIds = new ArrayList<>();
    private LocalDateTime endTime;

    public Epic(String name, String description) {
        super(name, description);
    }

    public ArrayList<Integer> getSubtaskIds() {
        return subtaskIds;
    }

    @Override
    public TaskType getType() {
        return TaskType.EPIC;
    }

    @Override
    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public void calculateTimeFields(ArrayList<Subtask> subtasks) {
        if (subtasks.isEmpty()) {
            this.duration = null;
            this.startTime = null;
            this.endTime = null;
            return;
        }

        LocalDateTime earliestStart = LocalDateTime.MAX;
        LocalDateTime latestEnd = LocalDateTime.MIN;
        Duration totalDuration = Duration.ZERO;

        for (Subtask subtask : subtasks) {
            if (subtask.getStartTime() != null && subtask.getDuration() != null) {
                LocalDateTime subtaskStart = subtask.getStartTime();
                LocalDateTime subtaskEnd = subtask.getEndTime();

                if (subtaskStart.isBefore(earliestStart)) {
                    earliestStart = subtaskStart;
                }

                if (subtaskEnd.isAfter(latestEnd)) {
                    latestEnd = subtaskEnd;
                }

                totalDuration = totalDuration.plus(subtask.getDuration());
            }
        }

        if (earliestStart.equals(LocalDateTime.MAX)) {
            this.startTime = null;
            this.endTime = null;
            this.duration = null;
        } else {
            this.startTime = earliestStart;
            this.endTime = latestEnd;
            this.duration = totalDuration;
        }
    }
}