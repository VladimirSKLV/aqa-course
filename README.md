# AQA Course (Java/Kotlin) — каркас проекта

Десктоп-приложение для обучения Java и Kotlin для начинающих AQA.  
Стек: **Java 17**, **Gradle**, **Kotlin 1.9.\***, GUI на **JavaFX**.

## Модули

- `:app` — JavaFX UI, навигация, отображение уроков, выполнение ДЗ (quiz).
- `:engine` — модели уроков/заданий, загрузка уроков из ресурсов, хранение прогресса.
- `:content` — контент уроков (YAML + Markdown) как ресурсы.
- `:runner` — заглушка под будущий запуск/проверку кода (Java/Kotlin + JUnit).

## Требования для разработки

- JDK 17 (например Temurin 17)
- Gradle

> Примечание: Gradle Wrapper файлы не включены (нужен бинарный `gradle-wrapper.jar`).
> Если у вас нет установленного Gradle, установите Gradle или сгенерируйте wrapper:
>
> `gradle wrapper --gradle-version 8.7`

## Запуск во время разработки

### 1) Запуск UI

Из корня проекта:

- Через установленный Gradle:
  - `gradle :app:run`

- Через wrapper (если вы его сгенерировали):
  - `./gradlew :app:run` (Linux/macOS)
  - `gradlew.bat :app:run` (Windows)

### 2) Сборка fat JAR (для тестирования как JAR)

- `gradle :app:shadowJar`

Результат:
- `app/build/libs/aqa-course-app-all.jar`

Запуск:
- `java -jar app/build/libs/aqa-course-app-all.jar`

## Где хранится прогресс

Прогресс сохраняется локально:
- Windows: `%USERPROFILE%\.aqa-course\progress.json`
- Linux/macOS: `~/.aqa-course/progress.json`

Удалите этот файл, чтобы сбросить прогресс.

## Контент уроков

См. `content/src/main/resources/lessons/`

- `lessons/index.yml` — индекс уроков (список путей к YAML уроков).
- `lessons/java/001/lesson.yml` — описание урока (метаданные + quiz).
- `lessons/java/001/theory.md` — теория в Markdown.

## Как добавить новый урок (кратко)

1) Создать папку `content/src/main/resources/lessons/<language>/<NNN>/`
2) Добавить `lesson.yml` + `theory.md`
3) Добавить путь урока в `lessons/index.yml`
4) Перезапустить `:app:run`

## Следующие шаги (архитектурно)

- Реализовать `code`-ДЗ: шаблоны файлов + скрытые JUnit-тесты-валидаторы в sandbox.
- Добавить терминал для stdout/stderr выполнения.
- Добавить редактор кода (Monaco в WebView или RichTextFX).


## Важно про запуск JAR (JavaFX)

Если при `java -jar ...` вы видите ошибку `JavaFX runtime components are missing`, значит JavaFX зависимости не попали в classpath/uber-jar. В модуле `app` уже добавлены platform-specific JavaFX runtime зависимости (win/linux/mac). Пересоберите:

```bash
gradle :app:shadowJar
```

И запускайте:

```bash
java -jar app/build/libs/aqa-course-app-all.jar
```
