package manager;

import tasks.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InMemoryHistoryManager implements HistoryManager {

    private static class Node {
        Task task;
        Node prev;
        Node next;

        Node(Task task) {
            this.task = task;
        }
    }

    private final Map<Integer, Node> nodeMap = new HashMap<>();

    private Node head;
    private Node tail;

    @Override
    public void add(Task task) {
        if (task == null) {
            return;
        }

        removeNode(nodeMap.get(task.getId()));

        linkLast(task);
    }

    @Override
    public void remove(int id) {  // ✅ Правильная сигнатура метода
        removeNode(nodeMap.get(id));
    }

    @Override
    public List<Task> getHistory() {
        return getTasks();
    }

    private void linkLast(Task task) {
        Node newNode = new Node(task);

        if (tail == null) {
            head = tail = newNode;
        } else {
            tail.next = newNode;
            newNode.prev = tail;
            tail = newNode;
        }

        // Обновляем HashMap
        nodeMap.put(task.getId(), newNode);
    }

    private void removeNode(Node node) {
        if (node == null) {
            return;
        }

        // Удаляем из HashMap
        nodeMap.remove(node.task.getId());

        // Обновляем связи в списке
        if (node.prev != null) {
            node.prev.next = node.next;
        } else {
            // Удаляем первый элемент
            head = node.next;
        }

        if (node.next != null) {
            node.next.prev = node.prev;
        } else {
            // Удаляем последний элемент
            tail = node.prev;
        }
    }

    private List<Task> getTasks() {
        List<Task> tasks = new ArrayList<>();
        Node current = head;

        while (current != null) {
            tasks.add(current.task);
            current = current.next;
        }

        return tasks;
    }
}