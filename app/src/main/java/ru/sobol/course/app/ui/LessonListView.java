package ru.sobol.course.app.ui;

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
import ru.sobol.course.engine.model.CourseLanguage;
import ru.sobol.course.engine.model.Lesson;

import java.util.List;

public class LessonListView {
    private final Navigator nav;

    public LessonListView(Navigator nav) {
        this.nav = nav;
    }

    public Parent view() {
        CourseLanguage lang = nav.selectedLanguage();
        List<Lesson> lessons = nav.lessonRepository().listByLanguage(lang);

        Label title = new Label("Уроки: " + (lang == CourseLanguage.JAVA ? "Java" : "Kotlin"));
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
        back.setOnAction(e -> nav.showWelcome());

        Button open = new Button("Открыть");
        open.getStyleClass().add("primary");
        open.setDisable(lessons.isEmpty());
        open.setOnAction(e -> {
            Lesson sel = list.getSelectionModel().getSelectedItem();
            if (sel != null && isUnlocked(lessons, sel)) {
                nav.showLesson(sel.getId());
            }
        });

        HBox actions = new HBox(12, back, open);
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
            if (!nav.progress().isCompleted(l.getLanguage(), l.getId())) return false;
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

            List<Lesson> ordered = nav.lessonRepository().listByLanguage(item.getLanguage());
            boolean unlocked = true;
            for (Lesson l : ordered) {
                if (l.getOrder() >= item.getOrder()) break;
                if (!nav.progress().isCompleted(l.getLanguage(), l.getId())) { unlocked = false; break; }
            }

            boolean done = nav.progress().isCompleted(item.getLanguage(), item.getId());

            String status = done ? "[DONE]" : (unlocked ? "[OPEN]" : "[LOCKED]");
            setText(status + " " + item.getOrder() + ". " + item.getTitle());
            setDisable(!unlocked);
        }
    }
}
