import manager.TaskManager;
import tasks.*;

public class Main {
    public static void main(String[] args) {
        TaskManager manager = new TaskManager();

        Task task1 = new Task("Переезд", "Собрать вещи");
        Task task2 = new Task("Покупка мебели", "Выбрать и купить диван");
        manager.addTask(task1);
        manager.addTask(task2);

        Epic epic1 = new Epic("Организовать праздник", "Подготовка к мероприятию");
        manager.addEpic(epic1);

        Subtask sub1 = new Subtask("Купить еду", "Список продуктов", Status.NEW, epic1.getId());
        Subtask sub2 = new Subtask("Заказать торт", "Позвонить в пекарню", Status.NEW, epic1.getId());
        manager.addSubtask(sub1);
        manager.addSubtask(sub2);

        Epic epic2 = new Epic("Купить квартиру", "Выбор и оформление");
        manager.addEpic(epic2);
        Subtask sub3 = new Subtask("Посмотреть объявления", "На Авито и Циан", Status.NEW, epic2.getId());
        manager.addSubtask(sub3);

        System.out.println("== Эпики ==");
        System.out.println(manager.getAllEpics());

        System.out.println("== Задачи ==");
        System.out.println(manager.getAllTasks());

        System.out.println("== Подзадачи ==");
        System.out.println(manager.getAllSubtasks());

        sub1.setStatus(Status.DONE);
        sub2.setStatus(Status.DONE);
        manager.updateSubtask(sub1);
        manager.updateSubtask(sub2);

        System.out.println("== Эпики после обновления статусов подзадач ==");
        System.out.println(manager.getEpicById(epic1.getId()));

        manager.deleteTaskById(task1.getId());
        manager.deleteEpicById(epic2.getId());

        System.out.println("== После удаления задачи и эпика ==");
        System.out.println("Задачи: " + manager.getAllTasks());
        System.out.println("Эпики: " + manager.getAllEpics());
    }
}