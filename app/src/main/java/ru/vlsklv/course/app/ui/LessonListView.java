package ru.vlsklv.course.app.ui;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import ru.vlsklv.course.app.ui.kit.AppButton;
import ru.vlsklv.course.app.ui.kit.AppCard;
import ru.vlsklv.course.engine.model.CourseLanguage;
import ru.vlsklv.course.engine.model.CourseTrack;
import ru.vlsklv.course.engine.model.Lesson;

import java.util.List;

public class LessonListView {
    private final Navigator nav;

    public LessonListView(Navigator nav) {
        this.nav = nav;
    }

    public Parent view() {
        CourseLanguage lang = nav.selectedLanguage();
        CourseTrack track = nav.selectedTrack();
        if (lang == null || track == null) {
            Label err = new Label("Не выбран язык или уровень курса.");
            err.getStyleClass().add("error");

            var back = AppButton.secondary("← На главный экран", e -> nav.showWelcome());

            var box = new VBox(12, err, back);
            box.setPadding(new Insets(18));
            return box;
        }

        List<Lesson> lessons = nav.lessonRepository().listByLanguageAndTrack(lang, track);

        String trackTitle = (track == CourseTrack.BEGINNER) ? "Начинающий" : "Продвинутый";
        Label title = new Label("📚 Уроки: " + (lang == CourseLanguage.JAVA ? "Java" : "Kotlin") + " — " + trackTitle);
        title.getStyleClass().add("h2");

        if (lessons.isEmpty()) {
            Label empty = new Label("Пока нет уроков для этого направления.\nДобавьте уроки в content и обновите lessons/index.yml.");
            empty.getStyleClass().add("muted");
            empty.setWrapText(true);
            empty.setAlignment(Pos.CENTER);
            empty.setMaxWidth(Double.MAX_VALUE);

            var back = AppButton.secondary("← Назад", e -> nav.showTrackSelect());
            var sandbox = AppButton.secondary("🧪 Песочница", e -> nav.showSandbox());

            HBox actions = new HBox(12, back, sandbox);
            actions.setAlignment(Pos.CENTER);

            VBox inner = new VBox(14, title, empty, actions);
            inner.setAlignment(Pos.CENTER);
            inner.setPadding(new Insets(8));

            AppCard card = new AppCard(inner);
            card.setMaxWidth(820);

            BorderPane pane = new BorderPane();
            pane.setPadding(new Insets(28));
            pane.setCenter(card);
            return pane;
        }

        Label hint = new Label("Уроки открываются последовательно: пока не пройден предыдущий — следующий недоступен.");
        hint.getStyleClass().add("muted");
        hint.setWrapText(true);

        ListView<Lesson> list = new ListView<>();
        list.setItems(FXCollections.observableArrayList(lessons));
        list.setCellFactory(v -> new LessonCell(nav));
        list.getStyleClass().add("lesson-list");
        list.setPrefHeight(560);

        var back = AppButton.ghost("← Назад", e -> nav.showTrackSelect());

        var resume = AppButton.secondary("▶ Продолжить", e -> {
            String resumeId = nav.resolveResumeLessonId(lang, track);
            if (resumeId != null) nav.showLesson(resumeId);
        });
        resume.setDisable(nav.resolveResumeLessonId(lang, track) == null);

        var open = AppButton.primary("📘 Открыть", e -> {
            Lesson sel = list.getSelectionModel().getSelectedItem();
            if (sel != null && isUnlocked(lessons, sel)) {
                nav.showLesson(sel.getId());
            }
        });

        HBox actions = new HBox(12, back, resume, open);
        actions.setAlignment(Pos.CENTER_RIGHT);

        BorderPane pane = new BorderPane();
        pane.setPadding(new Insets(18));
        pane.setTop(new VBoxHeader(title, hint).view());
        pane.setCenter(list);
        pane.setBottom(actions);
        BorderPane.setMargin(actions, new Insets(12, 0, 0, 0));
        return pane;
    }

    private boolean isUnlocked(List<Lesson> ordered, Lesson lesson) {
        for (Lesson l : ordered) {
            if (l.getOrder() >= lesson.getOrder()) break;
            if (!nav.progress().isCompleted(l.getLanguage(), nav.selectedTrack(), l.getId())) return false;
        }
        return true;
    }

    static class LessonCell extends ListCell<Lesson> {
        private final Navigator nav;

        private LessonCell(Navigator nav) {
            this.nav = nav;
        }

        @Override
        protected void updateItem(Lesson item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setText(null);
                setDisable(false);
                return;
            }

            List<Lesson> ordered = nav.lessonRepository().listByLanguageAndTrack(item.getLanguage(), nav.selectedTrack());
            boolean unlocked = true;
            for (Lesson l : ordered) {
                if (l.getOrder() >= item.getOrder()) break;
                if (!nav.progress().isCompleted(l.getLanguage(), nav.selectedTrack(), l.getId())) {
                    unlocked = false;
                    break;
                }
            }

            boolean done = nav.progress().isCompleted(item.getLanguage(), nav.selectedTrack(), item.getId());

            String status = done ? "✅" : (unlocked ? "▶" : "🔒");
            setText(status + "  " + item.getOrder() + ". " + item.getTitle());
            setWrapText(true);
            setPadding(new Insets(0, 0, 10, 0));
            setDisable(!unlocked && !done);
            setOpacity((!unlocked && !done) ? 0.60 : 1.0);
        }
    }
}
