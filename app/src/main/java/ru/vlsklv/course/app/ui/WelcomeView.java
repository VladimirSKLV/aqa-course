package ru.vlsklv.course.app.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import ru.vlsklv.course.engine.model.CourseLanguage;

public class WelcomeView {
    private final Navigator nav;

    public WelcomeView(Navigator nav) {
        this.nav = nav;
    }

    public Parent view() {
        Label title = new Label("Добро пожаловать в курс AQA по Java и Kotlin");
        title.getStyleClass().add("h1");
        title.setWrapText(true);
        title.setTextAlignment(TextAlignment.CENTER);
        title.setMaxWidth(520);

        Label subtitle = new Label("Выберите язык, затем уровень (начинающий/продвинутый). Уроки открываются последовательно.");
        subtitle.getStyleClass().add("muted");
        subtitle.setWrapText(true);
        subtitle.setTextAlignment(TextAlignment.CENTER);
        subtitle.setMaxWidth(520);

        Button resume = new Button("Продолжить обучение");
        resume.getStyleClass().add("primary");
        resume.setVisible(nav.hasResume());
        resume.setManaged(nav.hasResume());
        resume.setOnAction(e -> nav.resumeLast());

        Button javaBtn = new Button("Java");
        javaBtn.getStyleClass().add("primary");
        javaBtn.setOnAction(e -> {
            nav.selectLanguage(CourseLanguage.JAVA);
            nav.showTrackSelect();
        });

        Button kotlinBtn = new Button("Kotlin");
        kotlinBtn.getStyleClass().add("secondary");
        kotlinBtn.setOnAction(e -> {
            nav.selectLanguage(CourseLanguage.KOTLIN);
            nav.showTrackSelect();
        });

        VBox box = new VBox(16, title, subtitle, resume, javaBtn, kotlinBtn);
        box.setPadding(new Insets(28));
        box.setAlignment(Pos.CENTER);
        box.setMaxWidth(560);
        box.getStyleClass().add("card");

        VBox page = new VBox(box);
        page.setAlignment(Pos.CENTER);
        page.setPadding(new Insets(24));
        return page;
    }
}
