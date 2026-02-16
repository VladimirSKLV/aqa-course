package ru.vlsklv.course.app.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import ru.vlsklv.course.app.ui.kit.AppButton;
import ru.vlsklv.course.engine.model.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

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

        Label title = new Label("🧩 Домашнее задание: тест");
        title.getStyleClass().add("h2");

        Label subtitle = new Label("Порог прохождения: " + quiz.getPassPercent() + "%");
        subtitle.getStyleClass().add("muted");

        VBox questionsBox = new VBox(14);
        questionsBox.setPadding(new Insets(12));

        List<QuestionBlock> blocks = new ArrayList<>();
        int i = 1;
        for (QuizQuestion q : quiz.getQuestions()) {
            QuestionBlock qb = new QuestionBlock(i, q);
            blocks.add(qb);
            questionsBox.getChildren().add(qb.root);
            i++;
        }

        ScrollPane scroll = new ScrollPane(questionsBox);
        scroll.setFitToWidth(true);

        Label result = new Label("");
        result.getStyleClass().addAll("status-bar", "muted");
        result.setWrapText(true);

        var back = AppButton.secondary("← Назад к теории", e -> nav.showLesson(lessonId));
        var check = AppButton.primary("✅ Проверить", null);
        var toList = AppButton.ghost("📚 К списку уроков", e -> nav.showLessonList());

        check.setOnAction(e -> {
            int total = blocks.size();
            int correctCount = 0;

            for (QuestionBlock qb : blocks) {
                if (qb.isAnsweredCorrectly()) correctCount++;
            }

            int percent = total == 0 ? 0 : (int) Math.round((correctCount * 100.0) / total);
            boolean pass = percent >= quiz.getPassPercent();

            for (QuestionBlock qb : blocks) qb.showReview();
            for (QuestionBlock qb : blocks) qb.lock();
            check.setDisable(true);

            if (pass) {
                CourseTrack track = Objects.requireNonNull(nav.selectedTrack(), "selectedTrack");
                nav.progress().markCompleted(lesson.getLanguage(), track, lesson.getId());
                nav.saveProgress();

                result.getStyleClass().removeAll("error", "muted");
                if (!result.getStyleClass().contains("success")) result.getStyleClass().add("success");
                result.setText("Результат: " + percent + "% (" + correctCount + "/" + total + "). Тест пройден. Следующий урок станет доступен.");
            } else {
                result.getStyleClass().removeAll("success", "muted");
                if (!result.getStyleClass().contains("error")) result.getStyleClass().add("error");
                result.setText("Результат: " + percent + "% (" + correctCount + "/" + total + "). Недостаточно. Нужно минимум " + quiz.getPassPercent() + "%.");
            }
        });

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

    private static final class QuestionBlock {
        private final VBox root;
        private final ToggleGroup group = new ToggleGroup();
        private final List<OptionRow> options = new ArrayList<>();

        private QuestionBlock(int number, QuizQuestion q) {
            Label qLabel = new Label(number + ") " + q.getText());
            qLabel.getStyleClass().add("q-title");
            qLabel.setWrapText(true);

            List<QuizOption> shuffled = new ArrayList<>(q.getOptions());
            Collections.shuffle(shuffled);

            VBox optsBox = new VBox(6);
            for (QuizOption o : shuffled) {
                OptionRow row = new OptionRow(o, group);
                options.add(row);
                optsBox.getChildren().add(row.root);
            }

            root = new VBox(10, qLabel, optsBox);
            root.getStyleClass().add("q-block");
        }

        private boolean isAnsweredCorrectly() {
            Toggle sel = group.getSelectedToggle();
            if (sel == null) return false;
            Object ud = sel.getUserData();
            return ud instanceof QuizOption o && o.isCorrect();
        }

        private void showReview() {
            QuizOption selected = null;
            Toggle sel = group.getSelectedToggle();
            if (sel != null) {
                Object ud = sel.getUserData();
                if (ud instanceof QuizOption o) selected = o;
            }

            QuizOption correct = null;
            for (OptionRow r : options) {
                if (r.option.isCorrect()) { correct = r.option; break; }
            }

            for (OptionRow r : options) {
                r.clearReviewStyles();

                boolean isSelected = selected != null && r.option == selected;
                boolean isCorrect = r.option.isCorrect();

                if (isSelected && isCorrect) r.markSelectedCorrect();
                else if (isSelected) r.markSelectedWrong();
                else if (isCorrect) r.markMissedCorrect();

                boolean showExplanation;
                if (selected == null) {
                    showExplanation = isCorrect;
                } else if (isSelected) {
                    showExplanation = true;
                } else {
                    showExplanation = (!Objects.equals(selected, correct) && isCorrect);
                }

                if (showExplanation) r.showExplanation(isSelected && !isCorrect);
            }
        }

        private void lock() {
            for (OptionRow r : options) r.lock();
        }
    }

    private static final class OptionRow {
        private final VBox root;
        private final QuizOption option;
        private final RadioButton radio;
        private final Label explanation;

        private OptionRow(QuizOption option, ToggleGroup group) {
            this.option = option;

            this.radio = new RadioButton(option.getText());
            this.radio.setWrapText(true);
            this.radio.setUserData(option);
            this.radio.setToggleGroup(group);

            this.explanation = new Label();
            this.explanation.getStyleClass().add("opt-expl");
            this.explanation.setWrapText(true);
            this.explanation.setVisible(false);
            this.explanation.setManaged(false);

            this.root = new VBox(6, radio, explanation);
            this.root.getStyleClass().add("opt-row");
        }

        private void clearReviewStyles() {
            root.getStyleClass().removeAll("opt-selected-correct", "opt-selected-wrong", "opt-missed-correct");
            explanation.getStyleClass().removeAll("opt-expl-wrong");
            explanation.setText("");
            explanation.setVisible(false);
            explanation.setManaged(false);
        }

        private void markSelectedCorrect() {
            if (!root.getStyleClass().contains("opt-selected-correct")) root.getStyleClass().add("opt-selected-correct");
        }

        private void markSelectedWrong() {
            if (!root.getStyleClass().contains("opt-selected-wrong")) root.getStyleClass().add("opt-selected-wrong");
        }

        private void markMissedCorrect() {
            if (!root.getStyleClass().contains("opt-missed-correct")) root.getStyleClass().add("opt-missed-correct");
        }

        private void showExplanation(boolean wrongSelected) {
            String text = option.getExplanation();
            if (text == null || text.isBlank()) {
                text = option.isCorrect() ? "Правильный ответ." : "Неверный вариант.";
            }
            explanation.setText(text);
            if (wrongSelected && !explanation.getStyleClass().contains("opt-expl-wrong")) {
                explanation.getStyleClass().add("opt-expl-wrong");
            }
            explanation.setVisible(true);
            explanation.setManaged(true);
        }

        private void lock() {
            radio.setDisable(true);
        }
    }
}
