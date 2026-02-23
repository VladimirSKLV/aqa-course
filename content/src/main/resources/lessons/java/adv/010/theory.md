# Урок 10. Конфигурация BaseUrl

`baseUrl` — базовый адрес стенда, на который идут запросы/переходы.

Зачем выносить в конфиг:
- один и тот же набор тестов работает на dev/stage/prod-like;
- не нужно менять код при смене окружения;
- проще запускать в CI.

Пример через системное свойство:

```java
String baseUrl = System.getProperty("baseUrl", "https://api.test.local");
```

Пример запуска:

```bash
./gradlew test -DbaseUrl=https://api.example.com
```
