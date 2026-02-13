package ru.vlsklv.course.app.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import ru.vlsklv.course.app.sandbox.JavaSandboxRunner;
import ru.vlsklv.course.engine.model.CodeAssignment;
import ru.vlsklv.course.engine.model.CourseTrack;
import ru.vlsklv.course.engine.model.Lesson;

import java.time.Duration;
import java.util.Objects;

/**
 * Экран домашнего задания типа "code".
 *
 * MVP-реализация:
 * - редактор: обычный TextArea с моноширинным шрифтом
 * - запуск: компиляция + выполнение main-класса, вывод в "терминал"
 * - проверка: сравнение stdout с expectedStdout из задания
 */
public class CodeAssignmentView {
    private final Navigator nav;
    private final String lessonId;

    public CodeAssignmentView(Navigator nav, String lessonId) {
        this.nav = nav;
        this.lessonId = lessonId;
    }

    public Parent view() {
        Lesson lesson = nav.lessonRepository().findById(lessonId);
        if (lesson == null) {
            Label err = new Label("Урок не найден: " + lessonId);
            err.getStyleClass().add("error");
            BorderPane p = new BorderPane(err);
            p.setPadding(new Insets(18));
            return p;
        }
        if (!(lesson.getAssignment() instanceof CodeAssignment ca)) {
            Label err = new Label("Для урока пока нет code-задания.");
            err.getStyleClass().add("error");
            BorderPane p = new BorderPane(err);
            p.setPadding(new Insets(18));
            return p;
        }

        Label title = new Label("Домашнее задание: код");
        title.getStyleClass().add("h2");

        Label subtitle = new Label("Напишите/дополните код и нажмите \"Проверить\".");
        subtitle.getStyleClass().add("muted");

        String template = ca.getTemplate() == null ? null : nav.loader().readResourceText(ca.getTemplate());
        if (template == null) template = "// Template not found\n";

        TextArea editor = new TextArea(template);
        editor.getStyleClass().add("code-editor");
        editor.setWrapText(false);
        editor.setPrefRowCount(24);

        ScrollPane editorScroll = new ScrollPane(editor);
        editorScroll.setFitToWidth(true);
        editorScroll.setFitToHeight(true);

        Label termTitle = new Label("Вывод (терминал)");
        termTitle.getStyleClass().add("q-title");

        TextArea terminal = new TextArea();
        terminal.getStyleClass().add("terminal");
        terminal.setEditable(false);
        terminal.setWrapText(true);
        terminal.setPrefRowCount(10);

        Label result = new Label("");
        result.getStyleClass().add("muted");
        result.setWrapText(true);

        JavaSandboxRunner runner = new JavaSandboxRunner();

        Button back = new Button("Назад к теории");
        back.getStyleClass().add("secondary");
        back.setOnAction(e -> nav.showLesson(lessonId));

        Button run = new Button("Запустить");
        run.getStyleClass().add("secondary");

        Button check = new Button("Проверить");
        check.getStyleClass().add("primary");

        java.util.function.Supplier<JavaSandboxRunner.RunResult> doRun = () -> {
            terminal.clear();
            result.getStyleClass().removeAll("error", "success");
            result.getStyleClass().add("muted");
            result.setText("");

            JavaSandboxRunner.RunResult rr = runner.compileAndRun(
                    ca.getFileName(),
                    ca.getMainClass(),
                    editor.getText(),
                    Duration.ofSeconds(5)
            );

            if (rr.getStatus() == JavaSandboxRunner.RunResult.Status.OK) {
                terminal.appendText(rr.getStdout());
                if (rr.getStderr() != null && !rr.getStderr().isBlank()) {
                    terminal.appendText("\n[stderr]\n" + rr.getStderr());
                }
            } else {
                terminal.appendText(rr.getMessage());
            }

            return rr;
        };

        run.setOnAction(e -> doRun.get());

        check.setOnAction(e -> {
            JavaSandboxRunner.RunResult rr = doRun.get();
            if (rr.getStatus() != JavaSandboxRunner.RunResult.Status.OK) {
                result.getStyleClass().removeAll("muted");
                result.getStyleClass().add("error");
                result.setText("Код не прошёл компиляцию/запуск. Исправьте ошибки в терминале и повторите проверку.");
                return;
            }

            String expected = ca.getExpectedStdout();
            if (expected == null || expected.isBlank()) {
                result.getStyleClass().removeAll("muted");
                result.getStyleClass().add("success");
                result.setText("Код запущен. Для этого задания не задан expectedStdout, поэтому считается пройденным при успешном запуске.");
                markDone(lesson);
                check.setDisable(true);
                return;
            }

            // сравниваем только stdout
            String actual = rr.getStdout();
            String normExpected = normalizeForCompare(expected);
            String normActual = normalizeForCompare(actual);

            if (normExpected.equals(normActual)) {
                result.getStyleClass().removeAll("muted");
                result.getStyleClass().add("success");
                result.setText("Проверка пройдена. Следующий урок (если он существует) станет доступен.");
                markDone(lesson);
                check.setDisable(true);
            } else {
                result.getStyleClass().removeAll("muted");
                result.getStyleClass().add("error");
                result.setText(
                        "Проверка не пройдена. Ожидаемый вывод не совпал.\n\n" +
                                "Ожидалось:\n" + expected + "\n\n" +
                                "Получено:\n" + actual
                );
            }
        });

        Button toList = new Button("К списку уроков");
        toList.getStyleClass().add("secondary");
        toList.setOnAction(e -> nav.showLessonList());

        HBox actions = new HBox(12, back, run, check, toList);
        actions.setAlignment(Pos.CENTER_RIGHT);

        VBox center = new VBox(12, editorScroll, termTitle, terminal);
        center.setPadding(new Insets(12, 0, 0, 0));

        BorderPane pane = new BorderPane();
        pane.setPadding(new Insets(18));
        pane.setTop(new VBoxHeader(title, subtitle).view());
        pane.setCenter(center);

        VBox bottom = new VBox(10, result, actions);
        bottom.setAlignment(Pos.CENTER_RIGHT);
        pane.setBottom(bottom);
        BorderPane.setMargin(bottom, new Insets(12, 0, 0, 0));
        return pane;
    }

    private void markDone(Lesson lesson) {
        CourseTrack track = Objects.requireNonNull(nav.selectedTrack(), "selectedTrack");
        nav.progress().markCompleted(lesson.getLanguage(), track, lesson.getId());
        nav.saveProgress();
    }

    private static String normalizeForCompare(String text) {
        if (text == null) return "";
        String t = text.replace("\r\n", "\n").replace("\r", "\n");

        String[] lines = t.split("\n", -1);
        StringBuilder sb = new StringBuilder();
        for (String line : lines) {
            String rtrim = line.replaceAll("[ \t]+$", "");
            sb.append(rtrim).append("\n");
        }

        String res = sb.toString();
        while (res.endsWith("\n\n")) {
            res = res.substring(0, res.length() - 1);
        }
        return res.trim();
    }
}
