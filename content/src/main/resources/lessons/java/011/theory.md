# Урок 11. Отправка HTTP-запроса из приложения

Новый блок: **Первые интеграции: отправка тестовых запросов**.

В простом API-автотесте нужно:
1. Сформировать URL.
2. Отправить HTTP-запрос.
3. Проверить код ответа и тело.

Пример на Java HttpClient:

```java
HttpRequest request = HttpRequest.newBuilder(URI.create(baseUrl + "/health"))
        .GET()
        .build();
HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
```

Для стабильной практики в домашнем задании используем локальный встроенный сервер и проверяем ответы `200` и `404`.
