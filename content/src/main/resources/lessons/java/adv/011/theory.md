# Урок 11. Отправка HTTP-запроса из приложения

Начинаем блок: **Первые интеграции: отправка тестовых запросов**.

Базовый сценарий:
1. Собрать URL (`baseUrl + path`).
2. Отправить запрос (GET/POST).
3. Получить статус и тело ответа.

Мини-пример на Java 11 HttpClient:

```java
HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create(baseUrl + "/health"))
        .GET()
        .build();

HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
```

После отправки запроса мы можем валидировать статус и содержание ответа.
