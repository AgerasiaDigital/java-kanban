# java-kanban

Трекер задач с HTTP API

## Описание

Этот проект представляет собой систему управления задачами (Task Manager) с веб-интерфейсом через HTTP API. Поддерживает работу с обычными задачами, эпиками и подзадачами.

## Функциональность

- Создание, обновление, получение и удаление задач
- Работа с эпиками и подзадачами
- История просмотра задач
- Приоритизированный список задач
- Проверка пересечений задач по времени
- Сохранение и загрузка данных из файла

## HTTP API

Сервер запускается на порту 8080 и предоставляет следующие эндпоинты:

### Задачи (Tasks)
- `GET /tasks` - получить все задачи
- `GET /tasks/{id}` - получить задачу по ID
- `POST /tasks` - создать новую задачу или обновить существующую
- `DELETE /tasks/{id}` - удалить задачу

### Эпики (Epics)
- `GET /epics` - получить все эпики
- `GET /epics/{id}` - получить эпик по ID
- `GET /epics/{id}/subtasks` - получить подзадачи эпика
- `POST /epics` - создать новый эпик
- `DELETE /epics/{id}` - удалить эпик

### Подзадачи (Subtasks)
- `GET /subtasks` - получить все подзадачи
- `GET /subtasks/{id}` - получить подзадачу по ID
- `POST /subtasks` - создать новую подзадачу или обновить существующую
- `DELETE /subtasks/{id}` - удалить подзадачу

### История и приоритеты
- `GET /history` - получить историю просмотров
- `GET /prioritized` - получить задачи в порядке приоритета

## Коды ответов

- `200` - успешное выполнение запроса с данными
- `201` - успешное создание/обновление
- `404` - ресурс не найден
- `406` - задача пересекается с существующими
- `500` - внутренняя ошибка сервера

## Запуск

1. Убедитесь, что у вас установлена Java 21+
2. Добавьте библиотеку Gson в папку `lib/`
3. Скомпилируйте проект
4. Запустите `HttpTaskServer.main()`

Или просто выполните:

```bash
java -cp "lib/*:src" http.HttpTaskServer
```

## Тестирование

Для тестирования API можно использовать:
- Insomnia
- Postman  
- curl
- Встроенные unit-тесты

# java-kanban

Трекер задач с HTTP API

## Описание

Этот проект представляет собой систему управления задачами (Task Manager) с веб-интерфейсом через HTTP API. Поддерживает работу с обычными задачами, эпиками и подзадачами.

## Функциональность

- Создание, обновление, получение и удаление задач
- Работа с эпиками и подзадачами
- История просмотра задач
- Приоритизированный список задач
- Проверка пересечений задач по времени
- Сохранение и загрузка данных из файла

## HTTP API

Сервер запускается на порту 8080 и предоставляет следующие эндпоинты:

### Задачи (Tasks)
- `GET /tasks` - получить все задачи
- `GET /tasks/{id}` - получить задачу по ID
- `POST /tasks` - создать новую задачу или обновить существующую
- `DELETE /tasks/{id}` - удалить задачу

### Эпики (Epics)
- `GET /epics` - получить все эпики
- `GET /epics/{id}` - получить эпик по ID
- `GET /epics/{id}/subtasks` - получить подзадачи эпика
- `POST /epics` - создать новый эпик
- `DELETE /epics/{id}` - удалить эпик

### Подзадачи (Subtasks)
- `GET /subtasks` - получить все подзадачи
- `GET /subtasks/{id}` - получить подзадачу по ID
- `POST /subtasks` - создать новую подзадачу или обновить существующую
- `DELETE /subtasks/{id}` - удалить подзадачу

### История и приоритеты
- `GET /history` - получить историю просмотров
- `GET /prioritized` - получить задачи в порядке приоритета

## Коды ответов

- `200` - успешное выполнение запроса с данными
- `201` - успешное создание/обновление
- `404` - ресурс не найден
- `406` - задача пересекается с существующими
- `500` - внутренняя ошибка сервера

## Запуск

1. Убедитесь, что у вас установлена Java 21+
2. Добавьте библиотеку Gson в папку `lib/`
3. Скомпилируйте проект
4. Запустите `HttpTaskServer.main()`

Или просто выполните:

```bash
# Компиляция
javac -cp "lib/*:src" src/http/HttpTaskServer.java

# Запуск
java -cp "lib/*:src" http.HttpTaskServer
```

Сервер будет ожидать нажатия **Enter** для остановки.

## Тестирование

Для тестирования API можно использовать:
- Insomnia
- Postman  
- curl
- Встроенные unit-тесты

Пример запроса для создания задачи:

```bash
curl -X POST http://localhost:8080/tasks \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Новая задача",
    "description": "Описание задачи",
    "status": "NEW"
  }'
```

Пример получения всех задач:

```bash
curl http://localhost:8080/tasks
```

## Структура проекта

```
src/
├── http/
│   ├── HttpTaskServer.java          # Основной HTTP сервер
│   ├── adapters/
│   │   ├── LocalDateTimeAdapter.java # Адаптер для LocalDateTime
│   │   └── DurationAdapter.java      # Адаптер для Duration
│   └── handler/
│       ├── BaseHttpHandler.java     # Базовый обработчик
│       ├── TaskHandler.java         # Обработчик задач
│       ├── EpicHandler.java         # Обработчик эпиков
│       ├── SubtaskHandler.java      # Обработчик подзадач
│       ├── HistoryHandler.java      # Обработчик истории
│       └── PrioritizedHandler.java  # Обработчик приоритетов
├── manager/
│   ├── TaskManager.java             # Интерфейс менеджера
│   ├── InMemoryTaskManager.java     # Реализация в памяти
│   ├── FileBackedTaskManager.java   # Реализация с сохранением в файл
│   ├── HistoryManager.java          # Интерфейс истории
│   ├── InMemoryHistoryManager.java  # Реализация истории
│   ├── Managers.java                # Утилитарный класс
│   ├── NotFoundException.java       # Исключение "не найден"
│   ├── ManagerSaveException.java    # Исключение сохранения
│   └── CSVTaskFormatter.java        # Форматирование CSV
├── tasks/
│   ├── Task.java                    # Базовый класс задачи
│   ├── Epic.java                    # Эпик
│   ├── Subtask.java                 # Подзадача
│   ├── Status.java                  # Статусы задач
│   └── TaskType.java                # Типы задач
└── Main.java                        # Точка входа для консольного режима

test/
├── http/
│   ├── HttpTaskServerTest.java      # Общие тесты HTTP API
│   ├── TasksEndpointTest.java       # Тесты эндпоинта задач
│   ├── EpicsEndpointTest.java       # Тесты эндпоинта эпиков
│   ├── SubtasksEndpointTest.java    # Тесты эндпоинта подзадач
│   └── HistoryAndPrioritizedEndpointsTest.java # Тесты истории и приоритетов
├── manager/
│   ├── InMemoryTaskManagerTest.java
│   ├── FileBackedTaskManagerTest.java
│   ├── InMemoryHistoryManagerTest.java
│   ├── ManagersTest.java
│   ├── TaskManagerTest.java
│   ├── EpicStatusTest.java
│   └── InMemoryTaskManagerAdditionalTest.java
└── tasks/
    ├── TaskTest.java
    ├── EpicTest.java
    └── SubtaskTest.java
```

## Зависимости

- Java 21+
- Gson 2.10.1 (для сериализации JSON)
- JUnit 5.8.1 (для тестирования)

## Особенности реализации

1. **HTTP сервер** использует встроенный `com.sun.net.httpserver.HttpServer`
2. **JSON сериализация** через Gson с кастомными адаптерами для LocalDateTime и Duration
3. **Обработка исключений** с соответствующими HTTP кодами
4. **Проверка временных конфликтов** для задач с установленным временем
5. **История просмотров** реализована через двусвязный список
6. **Приоритизация задач** по времени начала
7. **Валидация данных** на уровне API
8. **Простая остановка** сервера по нажатию Enter

## Режимы запуска

### 1. HTTP API сервер
```bash
java -cp "lib/*:src" http.HttpTaskServer
```
Запускает веб-сервер на порту 8080. Для остановки нажмите Enter.

### 2. Консольная демонстрация
```bash
java -cp "lib/*:src" Main
```
Демонстрирует работу TaskManager через консоль.

## Разработка

Для добавления новых эндпоинтов:

1. Создайте новый обработчик, наследующийся от `BaseHttpHandler`
2. Зарегистрируйте его в `HttpTaskServer.setupHandlers()`
3. Добавьте соответствующие тесты

Для расширения функциональности менеджера задач:

1. Добавьте методы в интерфейс `TaskManager`
2. Реализуйте их в `InMemoryTaskManager`
3. При необходимости обновите `FileBackedTaskManager`
4. Добавьте тесты

## Примеры использования API

### Создание задачи с временем
```bash
curl -X POST http://localhost:8080/tasks \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Важная задача",
    "description": "Срочно выполнить",
    "status": "NEW",
    "duration": 60,
    "startTime": "2024-01-15 10:00:00"
  }'
```

### Создание эпика
```bash
curl -X POST http://localhost:8080/epics \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Большой проект",
    "description": "Описание проекта"
  }'
```

### Создание подзадачи
```bash
curl -X POST http://localhost:8080/subtasks \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Подзадача 1",
    "description": "Часть большого проекта",
    "status": "NEW",
    "epicId": 3
  }'
```

### Получение истории
```bash
curl http://localhost:8080/history
```

### Получение приоритизированных задач
```bash
curl http://localhost:8080/prioritized
```