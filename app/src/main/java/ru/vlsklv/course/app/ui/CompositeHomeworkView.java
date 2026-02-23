package ru.vlsklv.course.app.ui;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import ru.vlsklv.course.app.sandbox.JavaSandboxRunner;
import ru.vlsklv.course.app.ui.kit.AppButton;
import ru.vlsklv.course.app.ui.kit.AppPanel;
import ru.vlsklv.course.app.ui.kit.CodeUi;
import ru.vlsklv.course.engine.model.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

public class CompositeHomeworkView {
    private final Navigator nav;
    private final String lessonId;

    public CompositeHomeworkView(Navigator nav, String lessonId) {
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

        if (!(lesson.getAssignment() instanceof CompositeAssignment ca) || ca.getSteps() == null) {
            Label err = new Label("Для урока нет composite-задания.");
            err.getStyleClass().add("error");
            BorderPane p = new BorderPane(err);
            p.setPadding(new Insets(18));
            return p;
        }

        QuizAssignment quiz = null;
        CodeAssignment code = null;

        for (Assignment a : ca.getSteps()) {
            if (a instanceof QuizAssignment q && quiz == null) quiz = q;
            if (a instanceof CodeAssignment c && code == null) code = c;
        }

        if (quiz == null || code == null) {
            Label err = new Label("Composite-задание должно содержать quiz и code шаги.");
            err.getStyleClass().add("error");
            BorderPane p = new BorderPane(err);
            p.setPadding(new Insets(18));
            return p;
        }

        Label title = new Label("Домашнее задание");
        title.getStyleClass().add("h2");

        Label subtitle = new Label("Шаг 1: тест (80%+). Шаг 2: код (ожидаемый вывод).");
        subtitle.getStyleClass().add("muted");
        subtitle.setWrapText(true);

        TabPane tabs = new TabPane();
        tabs.getStyleClass().add("app-tabs");

        Tab quizTab = new Tab("Тест");
        quizTab.setClosable(false);

        Tab codeTab = new Tab("Код");
        codeTab.setClosable(false);
        codeTab.setDisable(true);

        tabs.getTabs().addAll(quizTab, codeTab);

        VBox quizRoot = new VBox(12);
        quizRoot.setPadding(new Insets(12));

        Label quizStatus = new Label("");
        quizStatus.getStyleClass().addAll("status-bar", "muted");
        quizStatus.setWrapText(true);

        List<QuizQuestion> questions = quiz.getQuestions() == null ? List.of() : quiz.getQuestions();
        List<QuestionBlock> blocks = new ArrayList<>();

        VBox questionsBox = new VBox(12);

        for (int i = 0; i < questions.size(); i++) {
            QuizQuestion q = questions.get(i);

            // Перемешиваем варианты при каждом открытии экрана
            List<QuizOption> opts = new ArrayList<>(q.getOptions() == null ? List.of() : q.getOptions());
            Collections.shuffle(opts, ThreadLocalRandom.current());

            QuestionBlock qb = new QuestionBlock(i + 1, q.getText(), opts);
            blocks.add(qb);
            questionsBox.getChildren().add(qb.root);
        }

        ScrollPane scroll = new ScrollPane(questionsBox);
        scroll.setFitToWidth(true);
        scroll.getStyleClass().add("app-scroll");

        AppPanel quizPanel = new AppPanel(new Label("Тест"), scroll);
        VBox.setVgrow(quizPanel, Priority.ALWAYS);

        var quizCheck = AppButton.primary("Проверить тест", null);

        QuizAssignment finalQuiz = quiz;
        quizCheck.setOnAction(e -> {
            quizStatus.getStyleClass().removeAll("error", "success");
            if (!quizStatus.getStyleClass().contains("muted")) quizStatus.getStyleClass().add("muted");
            quizStatus.setText("");

            int total = blocks.size();
            if (total == 0) {
                quizStatus.getStyleClass().removeAll("muted");
                quizStatus.getStyleClass().add("error");
                quizStatus.setText("В тесте нет вопросов.");
                return;
            }

            int correct = 0;

            // Показать разбор + заблокировать
            for (QuestionBlock b : blocks) {
                b.showReviewAndLock();
                if (b.isCorrect()) correct++;
            }

            int percent = (int) Math.round((correct * 100.0) / total);
            int pass = finalQuiz.getPassPercent();

            if (percent >= pass) {
                quizStatus.getStyleClass().removeAll("muted", "error");
                quizStatus.getStyleClass().add("success");
                quizStatus.setText("Тест пройден: " + percent + "% (" + correct + "/" + total + "). Открыт шаг «Код».");

                quizCheck.setDisable(true);
                codeTab.setDisable(false);
                tabs.getSelectionModel().select(codeTab);
            } else {
                quizStatus.getStyleClass().removeAll("muted", "success");
                quizStatus.getStyleClass().add("error");
                quizStatus.setText("Тест не пройден: " + percent + "% (" + correct + "/" + total + "). Нужно минимум " + pass + "%.");
            }
        });

        HBox quizActions = new HBox(12, quizCheck);
        quizActions.setAlignment(Pos.CENTER_RIGHT);

        quizRoot.getChildren().addAll(quizPanel, quizStatus, quizActions);
        VBox.setVgrow(quizPanel, Priority.ALWAYS);
        quizTab.setContent(quizRoot);

        String templateText = code.getTemplate() == null ? null : nav.loader().readResourceText(code.getTemplate());
        if (templateText == null) templateText = "// Template not found\n";

        CodeUi.EditorBundle bundle = CodeUi.createJavaEditor(templateText);
        bundle.scroll().setMaxWidth(Double.MAX_VALUE);

        Label editorTitle = new Label("Редактор");
        editorTitle.getStyleClass().add("panel-title");

        AppPanel editorPanel = new AppPanel(editorTitle, bundle.scroll());
        VBox.setVgrow(editorPanel, Priority.ALWAYS);

        Label termTitle = new Label("Вывод (терминал)");
        termTitle.getStyleClass().add("panel-title");

        TextArea terminal = new TextArea();
        terminal.getStyleClass().add("terminal");
        terminal.setEditable(false);
        terminal.setWrapText(true);

        AppPanel terminalPanel = new AppPanel(termTitle, terminal);
        VBox.setVgrow(terminal, Priority.ALWAYS);

        SplitPane split = new SplitPane(editorPanel, terminalPanel);
        split.setOrientation(Orientation.VERTICAL);
        split.setDividerPositions(0.72);

        Label codeStatus = new Label("");
        codeStatus.getStyleClass().addAll("status-bar", "muted");
        codeStatus.setWrapText(true);

        JavaSandboxRunner runner = new JavaSandboxRunner();

        Runnable resetCodeStatus = () -> {
            codeStatus.getStyleClass().removeAll("error", "success");
            if (!codeStatus.getStyleClass().contains("muted")) codeStatus.getStyleClass().add("muted");
            codeStatus.setText("");
        };

        var runBtn = AppButton.secondary("Запустить", null);
        var checkBtn = AppButton.primary("Проверить код", null);
        var clearBtn = AppButton.secondary("Очистить вывод", e -> terminal.clear());

        CodeAssignment finalCode = code;
        runBtn.setOnAction(e -> runAsync(bundle, runner, finalCode, terminal, codeStatus, resetCodeStatus, false, null));

        CodeAssignment finalCode1 = code;
        checkBtn.setOnAction(e -> runAsync(bundle, runner, finalCode1, terminal, codeStatus, resetCodeStatus, true, () -> {
            markDone(lesson);
            checkBtn.setDisable(true);
            runBtn.setDisable(true);
        }));

        HBox codeActions = new HBox(12, clearBtn, runBtn, checkBtn);
        codeActions.setAlignment(Pos.CENTER_RIGHT);

        VBox codeRoot = new VBox(12, split, codeStatus, codeActions);
        codeRoot.setPadding(new Insets(12));
        VBox.setVgrow(split, Priority.ALWAYS);
        codeTab.setContent(codeRoot);

        var backTheory = AppButton.ghost("Назад к теории", e -> nav.showLesson(lessonId));
        var toList = AppButton.secondary("К списку уроков", e -> nav.showLessonList());

        HBox pageActions = new HBox(12, backTheory, toList);
        pageActions.setAlignment(Pos.CENTER_RIGHT);

        BorderPane pane = new BorderPane();
        pane.setPadding(new Insets(18));
        pane.setTop(new VBoxHeader(title, subtitle).view());
        pane.setCenter(tabs);
        pane.setBottom(pageActions);
        BorderPane.setMargin(pageActions, new Insets(12, 0, 0, 0));

        Platform.runLater(bundle.editor()::requestFocus);
        return pane;
    }

    private void runAsync(
            CodeUi.EditorBundle bundle,
            JavaSandboxRunner runner,
            CodeAssignment ca,
            TextArea terminal,
            Label status,
            Runnable resetStatus,
            boolean isCheck,
            Runnable onCheckSuccess
    ) {
        bundle.hidePopup().run();
        terminal.clear();
        resetStatus.run();

        status.setText("Запуск...");
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

                if (!isCheck) {
                    status.getStyleClass().removeAll("muted", "error");
                    status.getStyleClass().add("success");
                    status.setText("Выполнено.");
                    return;
                }

                String expected = ca.getExpectedStdout();
                if (expected == null || expected.isBlank()) {
                    status.getStyleClass().removeAll("muted", "error");
                    status.getStyleClass().add("success");
                    status.setText("Код запущен. expectedStdout не задан — считаем шаг пройденным.");
                    if (onCheckSuccess != null) onCheckSuccess.run();
                    return;
                }

                String actual = rr.getStdout();
                String ne = normalizeForCompare(expected);
                String na = normalizeForCompare(actual);

                if (ne.equals(na)) {
                    status.getStyleClass().removeAll("muted", "error");
                    status.getStyleClass().add("success");
                    status.setText("Проверка пройдена. Урок закрыт как выполненный.");
                    if (onCheckSuccess != null) onCheckSuccess.run();
                } else {
                    status.getStyleClass().removeAll("muted", "success");
                    status.getStyleClass().add("error");
                    status.setText("Проверка не пройдена: вывод не совпал.");

                    terminal.appendText("\n\n---\nОжидалось:\n" + expected + "\n\nПолучено:\n" + actual);
                }
            } else {
                status.getStyleClass().removeAll("muted", "success");
                status.getStyleClass().add("error");
                status.setText("Ошибка.");

                terminal.appendText(rr.getMessage());
            }
        });

        task.setOnFailed(ev -> {
            status.getStyleClass().removeAll("muted", "success");
            status.getStyleClass().add("error");
            status.setText("Ошибка запуска.");

            Throwable ex = task.getException();
            terminal.appendText(ex == null ? "Unknown error" : ex.getMessage());
        });

        Thread t = new Thread(task, "composite-runner");
        t.setDaemon(true);
        t.start();
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

    private static final class QuestionBlock {
        final VBox root = new VBox(10);

        final Label title;
        final ToggleGroup group = new ToggleGroup();

        final List<OptionRow> rows = new ArrayList<>();
        final Label summary = new Label("");

        QuestionBlock(int idx, String text, List<QuizOption> options) {
            title = new Label(idx + ". " + (text == null ? "" : text));
            title.getStyleClass().add("q-title");
            title.setWrapText(true);

            VBox optsBox = new VBox(8);

            for (QuizOption o : options) {
                OptionRow r = new OptionRow(o, group);
                rows.add(r);
                optsBox.getChildren().add(r.root);
            }

            summary.getStyleClass().add("muted");
            summary.setWrapText(true);
            summary.setVisible(false);
            summary.setManaged(false);

            root.getChildren().addAll(title, optsBox, summary);
            root.getStyleClass().add("q-card");
        }

        boolean isCorrect() {
            Toggle t = group.getSelectedToggle();
            if (t == null) return false;
            Object ud = t.getUserData();
            return ud instanceof QuizOption o && o.isCorrect();
        }

        void showReviewAndLock() {
            Toggle selectedToggle = group.getSelectedToggle();
            QuizOption selected = null;

            if (selectedToggle != null) {
                Object ud = selectedToggle.getUserData();
                if (ud instanceof QuizOption o) selected = o;
            }

            QuizOption correct = null;
            for (OptionRow r : rows) {
                if (r.option.isCorrect()) {
                    correct = r.option;
                    break;
                }
            }

            // reset
            summary.setText("");
            summary.setVisible(true);
            summary.setManaged(true);

            for (OptionRow r : rows) {
                r.clearReview();
                r.lock();
            }

            // mark & explain
            for (OptionRow r : rows) {
                boolean isSelected = selected != null && r.option == selected;
                boolean isCorrect = r.option.isCorrect();

                if (isSelected && isCorrect) {
                    r.markSelectedCorrect();
                    r.showExplanation("✅ ", defaultExplanation(r.option, true));
                } else if (isSelected) {
                    r.markSelectedWrong();
                    r.showExplanation("❌ ", defaultExplanation(r.option, false));
                } else if (isCorrect) {
                    // правильный, но не выбран
                    r.markMissedCorrect();
                    r.showExplanation("✅ ", defaultExplanation(r.option, true));
                }
            }

            if (selected == null) {
                summary.setText("Ответ не выбран. Показан правильный вариант и объяснение.");
                return;
            }

            if (selected.isCorrect()) {
                summary.setText("Верно. Объяснение показано под выбранным вариантом.");
            } else {
                String correctText = correct == null ? "—" : safe(correct.getText());
                summary.setText("Неверно. Правильный ответ: " + correctText + ". Показаны объяснения для выбранного и правильного вариантов.");
            }
        }

        private static String defaultExplanation(QuizOption opt, boolean correct) {
            String e = opt.getExplanation();
            if (e != null && !e.isBlank()) return e;
            return correct ? "Это правильный вариант." : "Этот вариант неверный.";
        }

        private static String safe(String s) { return s == null ? "" : s; }
    }

    private static final class OptionRow {
        final VBox root = new VBox(6);
        final QuizOption option;
        final RadioButton rb;
        final Label expl = new Label("");

        OptionRow(QuizOption option, ToggleGroup group) {
            this.option = option;

            rb = new RadioButton(option.getText());
            rb.setToggleGroup(group);
            rb.setUserData(option);
            rb.getStyleClass().add("q-option");
            rb.setWrapText(true);

            expl.getStyleClass().add("q-expl");
            expl.setWrapText(true);
            expl.setVisible(false);
            expl.setManaged(false);

            root.getChildren().addAll(rb, expl);
        }

        void lock() {
            rb.setDisable(true);
        }

        void clearReview() {
            // Сбрасываем прошлые подсветки
            rb.getStyleClass().removeAll("correct", "wrong", "missed");
            root.getStyleClass().removeAll("opt-selected-correct", "opt-selected-wrong", "opt-missed-correct");

            expl.setText("");
            expl.setVisible(false);
            expl.setManaged(false);
            expl.getStyleClass().removeAll("q-expl-wrong");
        }

        void markSelectedCorrect() {
            if (!rb.getStyleClass().contains("correct")) rb.getStyleClass().add("correct");
            if (!root.getStyleClass().contains("opt-selected-correct")) root.getStyleClass().add("opt-selected-correct");
        }

        void markSelectedWrong() {
            if (!rb.getStyleClass().contains("wrong")) rb.getStyleClass().add("wrong");
            if (!root.getStyleClass().contains("opt-selected-wrong")) root.getStyleClass().add("opt-selected-wrong");
            if (!expl.getStyleClass().contains("q-expl-wrong")) expl.getStyleClass().add("q-expl-wrong");
        }

        void markMissedCorrect() {
            // Показываем правильный, но не выбранный — отдельно
            if (!rb.getStyleClass().contains("correct")) rb.getStyleClass().add("correct");
            if (!rb.getStyleClass().contains("missed")) rb.getStyleClass().add("missed");
            if (!root.getStyleClass().contains("opt-missed-correct")) root.getStyleClass().add("opt-missed-correct");
        }

        void showExplanation(String prefix, String text) {
            expl.setText((prefix == null ? "" : prefix) + (text == null ? "" : text));
            expl.setVisible(true);
            expl.setManaged(true);
        }
    }
}
