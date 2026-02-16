package ru.vlsklv.course.app.ui;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
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
import ru.vlsklv.course.engine.model.CourseLanguage;

public class SandboxView {
    private final Navigator nav;

    public SandboxView(Navigator nav) {
        this.nav = nav;
    }

    public Parent view() {
        Label title = new Label("Песочница");
        title.getStyleClass().add("h2");

        Label subtitle = new Label("Свободный режим: пишите код и запускайте. Подсказки: Ctrl+Space. Принятие: Tab/Enter.");
        subtitle.getStyleClass().add("muted");
        subtitle.setWrapText(true);

        ComboBox<CourseLanguage> lang = new ComboBox<>();
        lang.getStyleClass().add("app-combo");
        lang.getItems().addAll(CourseLanguage.JAVA, CourseLanguage.KOTLIN);
        lang.getSelectionModel().select(CourseLanguage.JAVA);
        lang.setVisibleRowCount(6);
        lang.setPrefWidth(220);
        lang.setMaxWidth(260);

        lang.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(CourseLanguage item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : prettyLang(item));
            }
        });
        lang.setCellFactory(cb -> new ListCell<>() {
            @Override
            protected void updateItem(CourseLanguage item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : prettyLang(item));
            }
        });

        Label langLbl = new Label("Язык:");
        langLbl.getStyleClass().add("muted");

        HBox langRow = new HBox(10, langLbl, lang);
        langRow.setAlignment(Pos.CENTER_LEFT);

        String javaTemplate =
                """
                public class Main {
                    public static void main(String[] args) {
                        System.out.println("Hello from sandbox");
                    }
                }
                """;

        CodeUi.EditorBundle bundle = CodeUi.createJavaEditor(javaTemplate);

        Label editorTitle = new Label("Редактор");
        editorTitle.getStyleClass().add("panel-title");

        VBox editorTop = new VBox(10, langRow, bundle.scroll());
        VBox.setVgrow(bundle.scroll(), Priority.ALWAYS);

        AppPanel editorPanel = new AppPanel(editorTitle, editorTop);
        VBox.setVgrow(editorPanel, Priority.ALWAYS);

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

        var run = AppButton.primary("Запустить", null);
        var clear = AppButton.secondary("Очистить вывод", e -> terminal.clear());
        var reset = AppButton.secondary("Шаблон", null);
        var back = AppButton.ghost("Назад", e -> nav.showWelcome());

        Runnable resetStatus = () -> {
            status.getStyleClass().removeAll("error", "success");
            if (!status.getStyleClass().contains("muted")) status.getStyleClass().add("muted");
            status.setText("");
        };

        reset.setOnAction(e -> {
            bundle.hidePopup().run();
            terminal.clear();
            resetStatus.run();

            if (lang.getValue() == CourseLanguage.JAVA) {
                bundle.editor().replaceText(javaTemplate);
            } else {
                bundle.editor().replaceText("// Kotlin sandbox: coming soon\n");
            }
            Platform.runLater(bundle.editor()::requestFocus);
        });

        lang.valueProperty().addListener((obs, o, n) -> {
            bundle.hidePopup().run();
            terminal.clear();
            resetStatus.run();

            if (n == CourseLanguage.JAVA) {
                bundle.editor().replaceText(javaTemplate);
                run.setDisable(false);
            } else {
                bundle.editor().replaceText("// Kotlin sandbox: запуск будет добавлен отдельным патчем\n");
                run.setDisable(true);

                status.getStyleClass().removeAll("muted", "success");
                if (!status.getStyleClass().contains("error")) status.getStyleClass().add("error");
                status.setText("Запуск Kotlin в песочнице ещё не реализован. Сейчас доступен только Java.");
            }
        });

        run.setOnAction(e -> {
            bundle.hidePopup().run();
            terminal.clear();
            resetStatus.run();

            status.setText("Запуск...");
            run.setDisable(true);

            Task<JavaSandboxRunner.RunResult> task = new Task<>() {
                @Override
                protected JavaSandboxRunner.RunResult call() {
                    return runner.compileAndRun(
                            "Main.java",
                            "Main",
                            bundle.editor().getText(),
                            java.time.Duration.ofSeconds(5)
                    );
                }
            };

            task.setOnSucceeded(ev -> {
                JavaSandboxRunner.RunResult rr = task.getValue();

                if (rr.getStatus() == JavaSandboxRunner.RunResult.Status.OK) {
                    status.getStyleClass().removeAll("muted", "error");
                    if (!status.getStyleClass().contains("success")) status.getStyleClass().add("success");
                    status.setText("Выполнено.");

                    terminal.appendText(rr.getStdout());
                    if (rr.getStderr() != null && !rr.getStderr().isBlank()) {
                        terminal.appendText("\n[stderr]\n" + rr.getStderr());
                    }
                } else {
                    status.getStyleClass().removeAll("muted", "success");
                    if (!status.getStyleClass().contains("error")) status.getStyleClass().add("error");
                    status.setText("Ошибка.");

                    terminal.appendText(rr.getMessage());
                }

                if (lang.getValue() == CourseLanguage.JAVA) run.setDisable(false);
            });

            task.setOnFailed(ev -> {
                Throwable ex = task.getException();

                status.getStyleClass().removeAll("muted", "success");
                if (!status.getStyleClass().contains("error")) status.getStyleClass().add("error");
                status.setText("Ошибка запуска.");

                terminal.appendText(ex == null ? "Unknown error" : ex.getMessage());

                if (lang.getValue() == CourseLanguage.JAVA) run.setDisable(false);
            });

            Thread t = new Thread(task, "sandbox-runner");
            t.setDaemon(true);
            t.start();
        });

        HBox actions = new HBox(12, back, reset, clear, run);
        actions.setAlignment(Pos.CENTER_RIGHT);

        SplitPane split = new SplitPane();
        split.setOrientation(Orientation.VERTICAL);
        split.getItems().addAll(editorPanel, terminalPanel);
        split.setDividerPositions(0.72);

        BorderPane pane = new BorderPane();
        pane.setPadding(new Insets(18));
        pane.setTop(new VBoxHeader(title, subtitle).view());
        pane.setCenter(split);

        VBox bottom = new VBox(10, status, actions);
        bottom.setAlignment(Pos.CENTER_RIGHT);
        pane.setBottom(bottom);
        BorderPane.setMargin(bottom, new Insets(12, 0, 0, 0));

        Platform.runLater(bundle.editor()::requestFocus);

        return pane;
    }

    private static String prettyLang(CourseLanguage l) {
        return switch (l) {
            case JAVA -> "☕ Java";
            case KOTLIN -> "🟣 Kotlin";
        };
    }
}
