package ru.sobol.course.app.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import ru.sobol.course.engine.model.Lesson;
import ru.sobol.course.engine.model.QuizAssignment;
import ru.sobol.course.engine.model.QuizOption;
import ru.sobol.course.engine.model.QuizQuestion;

import java.util.ArrayList;
import java.util.List;

public class QuizView {
    private final Navigator nav;
    private final String lessonId;

    public QuizView(Navigator nav, String lessonId) {
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
        if (!(lesson.getAssignment() instanceof QuizAssignment quiz)) {
            Label err = new Label("Для урока пока нет quiz-задания.");
            err.getStyleClass().add("error");
            BorderPane p = new BorderPane(err);
            p.setPadding(new Insets(18));
            return p;
        }

        Label title = new Label("Домашнее задание: тест");
        title.getStyleClass().add("h2");

        Label subtitle = new Label("Порог прохождения: " + quiz.getPassPercent() + "%");
        subtitle.getStyleClass().add("muted");

        VBox questionsBox = new VBox(14);
        questionsBox.setPadding(new Insets(12));

        List<ToggleGroup> groups = new ArrayList<>();

        int i = 1;
        for (QuizQuestion q : quiz.getQuestions()) {
            Label qLabel = new Label(i + ") " + q.getText());
            qLabel.getStyleClass().add("q-title");

            ToggleGroup tg = new ToggleGroup();
            groups.add(tg);

            VBox opts = new VBox(6);
            for (QuizOption o : q.getOptions()) {
                RadioButton rb = new RadioButton(o.getText());
                rb.setUserData(o.isCorrect());
                rb.setToggleGroup(tg);
                opts.getChildren().add(rb);
            }

            VBox block = new VBox(8, qLabel, opts);
            block.getStyleClass().add("q-block");
            questionsBox.getChildren().add(block);
            i++;
        }

        ScrollPane scroll = new ScrollPane(questionsBox);
        scroll.setFitToWidth(true);

        Label result = new Label("");
        result.getStyleClass().add("muted");

        Button back = new Button("Назад к теории");
        back.getStyleClass().add("secondary");
        back.setOnAction(e -> nav.showLesson(lessonId));

        Button check = new Button("Проверить");
        check.getStyleClass().add("primary");
        check.setOnAction(e -> {
            int total = groups.size();
            int correct = 0;

            for (ToggleGroup tg : groups) {
                Toggle sel = tg.getSelectedToggle();
                if (sel != null) {
                    Object ud = sel.getUserData();
                    if (ud instanceof Boolean b && b) correct++;
                }
            }

            int percent = total == 0 ? 0 : (int) Math.round((correct * 100.0) / total);
            boolean pass = percent >= quiz.getPassPercent();

            if (pass) {
                nav.progress().markCompleted(lesson.getLanguage(), lesson.getId());
                nav.saveProgress();
                result.getStyleClass().removeAll("error");
                if (!result.getStyleClass().contains("success")) result.getStyleClass().add("success");
                result.setText("Результат: " + percent + "% (" + correct + "/" + total + "). Тест пройден. Следующий урок (если он существует) станет доступен.");
            } else {
                result.getStyleClass().removeAll("success");
                if (!result.getStyleClass().contains("error")) result.getStyleClass().add("error");
                result.setText("Результат: " + percent + "% (" + correct + "/" + total + "). Недостаточно. Нужно минимум " + quiz.getPassPercent() + "%.");
            }
        });

        Button toList = new Button("К списку уроков");
        toList.getStyleClass().add("secondary");
        toList.setOnAction(e -> nav.showLessonList());

        HBox actions = new HBox(12, back, check, toList);
        actions.setAlignment(Pos.CENTER_RIGHT);

        BorderPane pane = new BorderPane();
        pane.setPadding(new Insets(18));
        pane.setTop(new VBoxHeader(title, subtitle).view());
        pane.setCenter(scroll);

        VBox bottom = new VBox(10, result, actions);
        bottom.setAlignment(Pos.CENTER_RIGHT);
        pane.setBottom(bottom);
        BorderPane.setMargin(bottom, new Insets(12, 0, 0, 0));
        return pane;
    }
}
