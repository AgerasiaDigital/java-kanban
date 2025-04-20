package tasks;

public class Subtask extends Task {
    private int epicId;

    public Subtask(String name, String description, Status status, int epicId) {
        super(name, description);
        this.status = status;
        this.epicId = epicId;
    }

    public int getEpicId() {
        return epicId;
    }

    public void setEpicId(int epicId) {
        this.epicId = epicId;
    }

    @Override
    public String toString() {
        return "Subtask{" +
                "id=" + id +
                ", name='" + name + ''' +
                ", status=" + status +
                ", epicId=" + epicId +
                '}';
    }
}