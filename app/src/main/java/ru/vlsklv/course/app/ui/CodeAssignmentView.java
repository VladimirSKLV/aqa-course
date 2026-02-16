package ru.vlsklv.course.app.ui;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import ru.vlsklv.course.app.sandbox.JavaSandboxRunner;
import ru.vlsklv.course.app.ui.kit.AppButton;
import ru.vlsklv.course.app.ui.kit.AppPanel;
import ru.vlsklv.course.app.ui.kit.CodeUi;
import ru.vlsklv.course.engine.model.CodeAssignment;
import ru.vlsklv.course.engine.model.CourseTrack;
import ru.vlsklv.course.engine.model.Lesson;

import java.util.Objects;
import java.util.function.BiConsumer;

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

        Label subtitle = new Label("Напишите/дополните код и нажмите \"Проверить\". Подсказки: Ctrl+Space. Принятие: Tab/Enter.");
        subtitle.getStyleClass().add("muted");
        subtitle.setWrapText(true);

        String template = ca.getTemplate() == null ? null : nav.loader().readResourceText(ca.getTemplate());
        if (template == null) template = "// Template not found\n";

        CodeUi.EditorBundle bundle = CodeUi.createJavaEditor(template);

        Label editorTitle = new Label("Редактор");
        editorTitle.getStyleClass().add("panel-title");

        AppPanel editorPanel = new AppPanel(editorTitle, bundle.scroll());
        AppPanel.grow(bundle.scroll());

        Label termTitle = new Label("Вывод (терминал)");
        termTitle.getStyleClass().add("panel-title");

        TextArea terminal = new TextArea();
        terminal.getStyleClass().add("terminal");
        terminal.setEditable(false);
        terminal.setWrapText(true);

        AppPanel terminalPanel = new AppPanel(termTitle, terminal);
        VBox.setVgrow(terminal, Priority.ALWAYS);

        Label status = new Label("");
        status.getStyleClass().addAll("status-bar", "muted");
        status.setWrapText(true);

        JavaSandboxRunner runner = new JavaSandboxRunner();

        var back = AppButton.secondary("Назад к теории", e -> nav.showLesson(lessonId));
        var toList = AppButton.ghost("К списку уроков", e -> nav.showLessonList());

        var run = AppButton.secondary("Запустить", null);
        var check = AppButton.primary("Проверить", null);

        SplitPane split = new SplitPane();
        split.setOrientation(Orientation.VERTICAL);
        split.getItems().addAll(editorPanel, terminalPanel);
        split.setDividerPositions(0.72);

        Runnable resetStatus = () -> {
            status.getStyleClass().removeAll("error", "success");
            if (!status.getStyleClass().contains("muted")) status.getStyleClass().add("muted");
            status.setText("");
        };

        Runnable setRunning = () -> {
            status.getStyleClass().removeAll("error", "success");
            if (!status.getStyleClass().contains("muted")) status.getStyleClass().add("muted");
            status.setText("Запуск...");
        };

        var doAsyncRun = new BiConsumer<Boolean, JavaSandboxRunner.RunResult>() {
            @Override
            public void accept(Boolean checkMode, JavaSandboxRunner.RunResult rr) {
                // не используется
            }
        };

        BiConsumer<Boolean, Runnable> startRun = (checkMode, afterUiUnlock) -> {
            terminal.clear();
            resetStatus.run();
            setRunning.run();
            bundle.hidePopup().run();

            run.setDisable(true);
            check.setDisable(true);

            Task<JavaSandboxRunner.RunResult> task = new Task<>() {
                @Override
                protected JavaSandboxRunner.RunResult call() {
                    return runner.compileAndRun(
                            ca.getFileName(),
                            ca.getMainClass(),
                            bundle.editor().getText(),
                            java.time.Duration.ofSeconds(5)
                    );
                }
            };

            task.setOnSucceeded(ev -> {
                JavaSandboxRunner.RunResult rr = task.getValue();

                if (rr.getStatus() == JavaSandboxRunner.RunResult.Status.OK) {
                    terminal.appendText(rr.getStdout());
                    if (rr.getStderr() != null && !rr.getStderr().isBlank()) {
                        terminal.appendText("\n[stderr]\n" + rr.getStderr());
                    }

                    if (!checkMode) {
                        status.getStyleClass().removeAll("muted", "error");
                        if (!status.getStyleClass().contains("success")) status.getStyleClass().add("success");
                        status.setText("Выполнено.");
                    } else {
                        String expected = ca.getExpectedStdout();
                        if (expected == null || expected.isBlank()) {
                            status.getStyleClass().removeAll("muted", "error");
                            if (!status.getStyleClass().contains("success")) status.getStyleClass().add("success");
                            status.setText("Код запущен. Для задания не задан expectedStdout — считается пройденным при успешном запуске.");
                            markDone(lesson);
                            check.setDisable(true);
                        } else {
                            String actual = rr.getStdout();
                            String normExpected = normalizeForCompare(expected);
                            String normActual = normalizeForCompare(actual);

                            if (normExpected.equals(normActual)) {
                                status.getStyleClass().removeAll("muted", "error");
                                if (!status.getStyleClass().contains("success")) status.getStyleClass().add("success");
                                status.setText("Проверка пройдена. Следующий урок станет доступен.");
                                markDone(lesson);
                                check.setDisable(true);
                            } else {
                                status.getStyleClass().removeAll("muted", "success");
                                if (!status.getStyleClass().contains("error")) status.getStyleClass().add("error");
                                status.setText(
                                        "Проверка не пройдена. Ожидаемый вывод не совпал.\n\n" +
                                                "Ожидалось:\n" + expected + "\n\n" +
                                                "Получено:\n" + actual
                                );
                            }
                        }
                    }
                } else {
                    terminal.appendText(rr.getMessage());
                    status.getStyleClass().removeAll("muted", "success");
                    if (!status.getStyleClass().contains("error")) status.getStyleClass().add("error");
                    status.setText("Код не прошёл компиляцию/запуск. Исправьте ошибки и повторите.");
                }

                run.setDisable(false);
                if (!check.isDisabled()) check.setDisable(false);
                if (afterUiUnlock != null) afterUiUnlock.run();
            });

            task.setOnFailed(ev -> {
                Throwable ex = task.getException();
                terminal.appendText(ex == null ? "Unknown error" : ex.getMessage());

                status.getStyleClass().removeAll("muted", "success");
                if (!status.getStyleClass().contains("error")) status.getStyleClass().add("error");
                status.setText("Ошибка запуска.");

                run.setDisable(false);
                if (!check.isDisabled()) check.setDisable(false);
                if (afterUiUnlock != null) afterUiUnlock.run();
            });

            Thread t = new Thread(task, "code-assignment-runner");
            t.setDaemon(true);
            t.start();
        };

        run.setOnAction(e -> startRun.accept(false, null));
        check.setOnAction(e -> startRun.accept(true, null));

        HBox actions = new HBox(12, back, toList, run, check);
        actions.setAlignment(Pos.CENTER_RIGHT);

        BorderPane pane = new BorderPane();
        pane.setPadding(new Insets(18));
        pane.setTop(new VBoxHeader(title, subtitle).view());
        pane.setCenter(split);

        VBox bottom = new VBox(10, status, actions);
        bottom.setAlignment(Pos.CENTER_RIGHT);
        pane.setBottom(bottom);
        BorderPane.setMargin(bottom, new Insets(12, 0, 0, 0));

        // небольшой фокус на редактор
        Platform.runLater(bundle.editor()::requestFocus);

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
