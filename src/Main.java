import manager.Managers;
import manager.TaskManager;
import tasks.*;

public class Main {
    public static void main(String[] args) {
        System.out.println("=== ДЕМОНСТРАЦИЯ ОБНОВЛЕННОГО ТРЕКЕРА ЗАДАЧ (Sprint 8) ===\n");

        TaskManager manager = Managers.getDefault();

        System.out.println("1. Создаем две задачи:");
        Task task1 = new Task("Переезд", "Собрать вещи и упаковать");
        Task task2 = new Task("Покупка мебели", "Выбрать и купить диван");
        manager.addTask(task1);
        manager.addTask(task2);
        System.out.println("   Задача 1: " + task1);
        System.out.println("   Задача 2: " + task2);

        System.out.println("\n2. Создаем эпик с тремя подзадачами:");
        Epic epic1 = new Epic("Организовать праздник", "Подготовка к мероприятию");
        manager.addEpic(epic1);

        Subtask sub1 = new Subtask("Купить еду", "Список продуктов", Status.NEW, epic1.getId());
        Subtask sub2 = new Subtask("Заказать торт", "Позвонить в пекарню", Status.NEW, epic1.getId());
        Subtask sub3 = new Subtask("Пригласить гостей", "Отправить приглашения", Status.NEW, epic1.getId());
        manager.addSubtask(sub1);
        manager.addSubtask(sub2);
        manager.addSubtask(sub3);

        System.out.println("   Эпик: " + epic1);
        System.out.println("   Подзадача 1: " + sub1);
        System.out.println("   Подзадача 2: " + sub2);
        System.out.println("   Подзадача 3: " + sub3);

        // 3. Создаем эпик без подзадач
        System.out.println("\n3. Создаем эпик без подзадач:");
        Epic epic2 = new Epic("Купить квартиру", "Выбор и оформление недвижимости");
        manager.addEpic(epic2);
        System.out.println("   Эпик: " + epic2);

        System.out.println("\n4. Запрашиваем задачи и проверяем историю:");

        System.out.println("\n   Запрашиваем task1:");
        manager.getTaskById(task1.getId());
        printHistory(manager);

        System.out.println("\n   Запрашиваем epic1:");
        manager.getEpicById(epic1.getId());
        printHistory(manager);

        System.out.println("\n   Запрашиваем sub1:");
        manager.getSubtaskById(sub1.getId());
        printHistory(manager);

        System.out.println("\n   Запрашиваем task2:");
        manager.getTaskById(task2.getId());
        printHistory(manager);

        System.out.println("\n   Запрашиваем sub2:");
        manager.getSubtaskById(sub2.getId());
        printHistory(manager);

        System.out.println("\n5. Проверяем удаление дубликатов:");
        System.out.println("\n   Повторно запрашиваем task1:");
        manager.getTaskById(task1.getId());
        printHistory(manager);

        System.out.println("\n   Повторно запрашиваем epic1:");
        manager.getEpicById(epic1.getId());
        printHistory(manager);

        System.out.println("\n6. Удаляем task2 и проверяем, что она исчезла из истории:");
        System.out.println("   История до удаления:");
        printHistory(manager);

        manager.deleteTaskById(task2.getId());
        System.out.println("   История после удаления task2:");
        printHistory(manager);

        System.out.println("\n7. Удаляем эпик с подзадачами и проверяем историю:");
        System.out.println("   История до удаления эпика:");
        printHistory(manager);

        manager.deleteEpicById(epic1.getId());
        System.out.println("   История после удаления эпика (должны исчезнуть эпик и все его подзадачи):");
        printHistory(manager);

        System.out.println("\n8. Демонстрируем неограниченность истории:");
        System.out.println("   Создаем и просматриваем 15 задач:");

        for (int i = 1; i <= 15; i++) {
            Task task = new Task("Задача " + i, "Описание задачи " + i);
            manager.addTask(task);
            manager.getTaskById(task.getId());
        }

        System.out.println("   Размер истории: " + manager.getHistory().size() + " (раньше был лимит в 10)");
        printHistory(manager);

        System.out.println("\n=== ДЕМОНСТРАЦИЯ ЗАВЕРШЕНА ===");
    }

    private static void printHistory(TaskManager manager) {
        System.out.println("   История просмотров:");
        var history = manager.getHistory();
        if (history.isEmpty()) {
            System.out.println("   (пусто)");
        } else {
            for (int i = 0; i < history.size(); i++) {
                System.out.println("   " + (i + 1) + ". " + history.get(i));
            }
        }
    }
}