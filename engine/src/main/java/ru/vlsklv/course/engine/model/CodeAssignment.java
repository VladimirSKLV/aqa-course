package ru.vlsklv.course.engine.model;

/**
 * Домашнее задание, в котором пользователь пишет код и запускает его.
 *
 * На первом этапе валидируем результат по stdout (сравнение с expectedStdout).
 * Дальше этот тип задания можно расширить до запуска скрытых JUnit-тестов.
 */
public class CodeAssignment implements Assignment {
    /**
     * Например: "JAVA" / "KOTLIN".
     * Сейчас используется только JAVA.
     */
    private String runner = "JAVA";

    /** Имя основного файла (например, Main.java). */
    private String fileName = "Main.java";

    /** Имя main-класса без пакета (например, Main). */
    private String mainClass = "Main";

    /** Путь к template-файлу в ресурсах (classpath). */
    private String template;

    /**
     * Ожидаемый stdout. Если пусто — задание считается пройденным при успешном запуске.
     */
    private String expectedStdout;

    public CodeAssignment() {}

    public String getRunner() { return runner; }
    public void setRunner(String runner) { this.runner = runner; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public String getMainClass() { return mainClass; }
    public void setMainClass(String mainClass) { this.mainClass = mainClass; }

    public String getTemplate() { return template; }
    public void setTemplate(String template) { this.template = template; }

    public String getExpectedStdout() { return expectedStdout; }
    public void setExpectedStdout(String expectedStdout) { this.expectedStdout = expectedStdout; }
}
