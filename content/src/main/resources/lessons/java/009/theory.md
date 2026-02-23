# Урок 9. Gradle/Maven в AQA-проекте

Оба сборщика решают задачи зависимостей и запуска тестов.

## Что обычно добавляют для beginner API/UI автотестов

### Gradle (пример)
```kotlin
dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
    testImplementation("io.rest-assured:rest-assured:5.5.0")
    testImplementation("org.seleniumhq.selenium:selenium-java:4.25.0")
    testRuntimeOnly("org.slf4j:slf4j-simple:2.0.16")
}
```

### Maven (пример)
- `junit-jupiter`
- `rest-assured`
- `selenium-java`

Важно: не подключайте всё подряд, держите минимально нужный набор библиотек.
