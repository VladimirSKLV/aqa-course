package ru.vlsklv.course.app.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import ru.vlsklv.course.app.ui.kit.AppButton;
import ru.vlsklv.course.app.ui.kit.AppCard;
import ru.vlsklv.course.engine.model.CourseLanguage;
import ru.vlsklv.course.engine.model.CourseTrack;

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

            var back = AppButton.secondary("На главный экран", e -> nav.showWelcome());

            VBox box = new VBox(12, err, back);
            box.setPadding(new Insets(18));
            return box;
        }

        String langTitle = (lang == CourseLanguage.JAVA) ? "Java" : "Kotlin";

        Label title = new Label("Выберите уровень — " + langTitle);
        title.getStyleClass().add("h2");

        Label subtitle = new Label("Начинающий: базовые основы языка и AQA. Продвинутый: углублённые темы, практики и архитектура тестов.");
        subtitle.getStyleClass().add("muted");
        subtitle.setWrapText(true);

        var beginner = AppButton.primary("Начинающий", e -> {
            nav.selectTrack(CourseTrack.BEGINNER);
            nav.showLessonList();
        });

        var advanced = AppButton.secondary("Продвинутый", e -> {
            nav.selectTrack(CourseTrack.ADVANCED);
            nav.showLessonList();
        });

        var back = AppButton.ghost("Назад", e -> nav.showWelcome());

        HBox levelBtns = new HBox(12, beginner, advanced);
        levelBtns.setAlignment(Pos.CENTER);

        VBox inner = new VBox(16, title, subtitle, levelBtns, back);
        inner.setAlignment(Pos.CENTER);

        AppCard card = new AppCard(inner);
        card.setMaxWidth(760);

        BorderPane page = new BorderPane();
        page.setPadding(new Insets(28));
        page.setCenter(card);
        return page;
    }
}
