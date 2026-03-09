package ru.vlsklv.course.app.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import ru.vlsklv.course.app.ui.kit.AppButton;
import ru.vlsklv.course.app.ui.kit.AppCard;
import ru.vlsklv.course.engine.model.CourseLanguage;

public class WelcomeView {
    private final Navigator nav;

    public WelcomeView(Navigator nav) {
        this.nav = nav;
    }

    public Parent view() {
        Label title = new Label("Курс AQA по Java и Kotlin");
        title.getStyleClass().add("h1");
        title.setWrapText(true);
        title.setTextAlignment(TextAlignment.CENTER);
        title.setAlignment(Pos.CENTER);
        title.setMaxWidth(Double.MAX_VALUE);

        Label subtitle = new Label("Выберите язык и уровень. Уроки открываются последовательно. Есть песочница и отдельный раздел API smoke-тестирования.");
        subtitle.getStyleClass().add("muted");
        subtitle.setWrapText(true);
        subtitle.setTextAlignment(TextAlignment.CENTER);
        subtitle.setAlignment(Pos.CENTER);
        subtitle.setMaxWidth(Double.MAX_VALUE);

        var resume = AppButton.primary("▶ Продолжить обучение", e -> nav.resumeLast());
        resume.setVisible(nav.hasResume());
        resume.setManaged(nav.hasResume());

        var javaBtn = AppButton.primary("☕ Java", e -> {
            nav.selectLanguage(CourseLanguage.JAVA);
            nav.showTrackSelect();
        });

        var kotlinBtn = AppButton.secondary("🟣 Kotlin", e -> {
            nav.selectLanguage(CourseLanguage.KOTLIN);
            nav.showTrackSelect();
        });

        var sandbox = AppButton.secondary("🧪 Песочница + API тестирование", e -> nav.showSandbox());

        HBox langRow = new HBox(12, javaBtn, kotlinBtn);
        langRow.setAlignment(Pos.CENTER);

        HBox quickRow = new HBox(12, sandbox);
        quickRow.setAlignment(Pos.CENTER);

        VBox content = new VBox(16, title, subtitle, resume, langRow, quickRow);
        content.setAlignment(Pos.CENTER);
        content.setFillWidth(true);

        AppCard card = new AppCard(content);
        card.setMaxWidth(820);

        VBox page = new VBox(card);
        page.setAlignment(Pos.CENTER);
        page.setPadding(new Insets(28));
        return page;
    }
}
