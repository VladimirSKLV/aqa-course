package ru.sobol.course.app.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import ru.sobol.course.engine.model.CourseLanguage;

public class WelcomeView {
    private final Navigator nav;

    public WelcomeView(Navigator nav) {
        this.nav = nav;
    }

    public Parent view() {
        Label title = new Label("Добро пожаловать в курс для начинающих AQA");
        title.getStyleClass().add("h1");

        Label subtitle = new Label("Выберите направление обучения:");
        subtitle.getStyleClass().add("muted");

        Button javaBtn = new Button("Java");
        javaBtn.getStyleClass().add("primary");
        javaBtn.setOnAction(e -> {
            nav.selectLanguage(CourseLanguage.JAVA);
            nav.showLessonList();
        });

        Button kotlinBtn = new Button("Kotlin");
        kotlinBtn.getStyleClass().add("secondary");
        kotlinBtn.setOnAction(e -> {
            nav.selectLanguage(CourseLanguage.KOTLIN);
            nav.showLessonList();
        });

        VBox box = new VBox(16, title, subtitle, javaBtn, kotlinBtn);
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
