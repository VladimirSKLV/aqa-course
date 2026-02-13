package ru.vlsklv.course.app.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import ru.vlsklv.course.engine.model.CourseLanguage;
import ru.vlsklv.course.engine.model.CourseTrack;

/**
 * Экран выбора уровня: начинающий/продвинутый.
 */
public class TrackSelectView {
    private final Navigator nav;

    public TrackSelectView(Navigator nav) {
        this.nav = nav;
    }

    public Parent view() {
        CourseLanguage lang = nav.selectedLanguage();
        if (lang == null) {
            Label err = new Label("Не выбран язык курса.");
            err.getStyleClass().add("error");

            Button back = new Button("На главный экран");
            back.getStyleClass().add("secondary");
            back.setOnAction(e -> nav.showWelcome());

            VBox box = new VBox(12, err, back);
            box.setPadding(new Insets(18));
            return box;
        }
        String langTitle = (lang == CourseLanguage.JAVA) ? "Java" : "Kotlin";

        Label title = new Label("Выберите уровень: " + langTitle);
        title.getStyleClass().add("h2");

        Label subtitle = new Label("Для начинающих — базовые основы языка и тестирования. Для продвинутых — углублённые темы и практики AQA.");
        subtitle.getStyleClass().add("muted");
        subtitle.setWrapText(true);

        Button beginner = new Button("Начинающий");
        beginner.getStyleClass().add("primary");
        beginner.setOnAction(e -> {
            nav.selectTrack(CourseTrack.BEGINNER);
            nav.showLessonList();
        });

        Button advanced = new Button("Продвинутый");
        advanced.getStyleClass().add("secondary");
        advanced.setOnAction(e -> {
            nav.selectTrack(CourseTrack.ADVANCED);
            nav.showLessonList();
        });

        Button back = new Button("Назад");
        back.getStyleClass().add("secondary");
        back.setOnAction(e -> nav.showWelcome());

        HBox levelBtns = new HBox(12, beginner, advanced);
        levelBtns.setAlignment(Pos.CENTER);

        VBox box = new VBox(16, title, subtitle, levelBtns, back);
        box.setPadding(new Insets(28));
        box.setAlignment(Pos.CENTER);
        box.setMaxWidth(640);
        box.getStyleClass().add("card");

        BorderPane page = new BorderPane();
        page.setPadding(new Insets(24));
        page.setCenter(box);
        return page;
    }
}
