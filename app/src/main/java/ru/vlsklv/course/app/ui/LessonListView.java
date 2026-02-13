package ru.vlsklv.course.app.ui;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
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

            Button back = new Button("На главный экран");
            back.getStyleClass().add("secondary");
            back.setOnAction(e -> nav.showWelcome());

            VBox box = new VBox(12, err, back);
            box.setPadding(new Insets(18));
            return box;
        }
        List<Lesson> lessons = nav.lessonRepository().listByLanguageAndTrack(lang, track);

        String trackTitle = (track == CourseTrack.BEGINNER) ? "Начинающий" : "Продвинутый";
        Label title = new Label("Уроки: " + (lang == CourseLanguage.JAVA ? "Java" : "Kotlin") + " — " + trackTitle);
        title.getStyleClass().add("h2");

        String extra = lessons.isEmpty()
                ? "Для этого направления пока нет уроков."
                : "Уроки открываются последовательно: пока не пройден предыдущий — следующий недоступен.";
        Label hint = new Label(extra);
        hint.getStyleClass().add("muted");

        ListView<Lesson> list = new ListView<>();
        list.setItems(FXCollections.observableArrayList(lessons));
        list.setCellFactory(v -> new LessonCell(nav));
        list.setPrefHeight(540);

        Button back = new Button("Назад");
        back.getStyleClass().add("secondary");
        back.setOnAction(e -> nav.showTrackSelect());

        Button resume = new Button("Продолжить");
        resume.getStyleClass().add("secondary");
        resume.setDisable(lessons.isEmpty() || nav.resolveResumeLessonId(lang, track) == null);
        resume.setOnAction(e -> {
            String resumeId = nav.resolveResumeLessonId(lang, track);
            if (resumeId != null) nav.showLesson(resumeId);
        });

        Button open = new Button("Открыть");
        open.getStyleClass().add("primary");
        open.setDisable(lessons.isEmpty());
        open.setOnAction(e -> {
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

    private static class LessonCell extends ListCell<Lesson> {
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

            // В этой вью мы показываем выбранную ветку, поэтому берём её.
            List<Lesson> ordered = nav.lessonRepository().listByLanguageAndTrack(item.getLanguage(), nav.selectedTrack());
            boolean unlocked = true;
            for (Lesson l : ordered) {
                if (l.getOrder() >= item.getOrder()) break;
                if (!nav.progress().isCompleted(l.getLanguage(), nav.selectedTrack(), l.getId())) { unlocked = false; break; }
            }

            boolean done = nav.progress().isCompleted(item.getLanguage(), nav.selectedTrack(), item.getId());

            String status = done ? "[DONE]" : (unlocked ? "[OPEN]" : "[LOCKED]");
            setText(status + " " + item.getOrder() + ". " + item.getTitle());
            setDisable(!unlocked);
        }
    }
}
