# Урок 10. Конфигурация BaseUrl

`baseUrl` нужен, чтобы запускать те же тесты на разных окружениях (dev/stage/demo).

Пример:

```java
String baseUrl = System.getProperty("baseUrl", "https://jsonplaceholder.typicode.com");
```

Запуск:

```bash
./gradlew test -DbaseUrl=https://my-stand.example.com
```

## Для API-тестов
- baseUrl + endpoint → полный URL.

## Для Web-тестов
- браузер открывает `baseUrl`, а тесты проверяют заголовок/элементы страницы.

Вынос baseUrl в конфиг — обязательная привычка для AQA.
