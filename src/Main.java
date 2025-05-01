import manager.Managers;
import manager.TaskManager;
import tasks.*;

public class Main {
    public static void main(String[] args) {
        TaskManager manager = Managers.getDefault();

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

        printAllTasks(manager);

        // Проверка истории
        System.out.println("\n== Проверяем историю просмотров ==");
        System.out.println("Получаем задачу 1");
        manager.getTaskById(task1.getId());
        printHistory(manager);

        System.out.println("\nПолучаем эпик 1");
        manager.getEpicById(epic1.getId());
        printHistory(manager);

        System.out.println("\nПолучаем подзадачу 1");
        manager.getSubtaskById(sub1.getId());
        printHistory(manager);

        System.out.println("\nПовторно получаем задачу 1");
        manager.getTaskById(task1.getId());
        printHistory(manager);

        // Тестирование длины истории
        System.out.println("\n== Проверяем длину истории (не более 10 элементов) ==");
        for (int i = 0; i < 10; i++) {
            manager.getTaskById(task1.getId());
            manager.getEpicById(epic1.getId());
        }
        printHistory(manager);
    }

    private static void printAllTasks(TaskManager manager) {
        System.out.println("Задачи:");
        for (Task task : manager.getAllTasks()) {
            System.out.println(task);
        }
        System.out.println("\nЭпики:");
        for (Epic epic : manager.getAllEpics()) {
            System.out.println(epic);

            for (Subtask task : manager.getSubtasksByEpicId(epic.getId())) {
                System.out.println("--> " + task);
            }
        }
        System.out.println("\nПодзадачи:");
        for (Task subtask : manager.getAllSubtasks()) {
            System.out.println(subtask);
        }
    }

    private static void printHistory(TaskManager manager) {
        System.out.println("История:");
        for (Task task : manager.getHistory()) {
            System.out.println(task);
        }
    }
}