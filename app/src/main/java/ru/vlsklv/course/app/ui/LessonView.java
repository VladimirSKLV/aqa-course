package ru.vlsklv.course.app.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.web.WebView;
import ru.vlsklv.course.app.ui.kit.AppButton;
import ru.vlsklv.course.app.util.MarkdownRenderer;
import ru.vlsklv.course.engine.model.Lesson;

public class LessonView {
    private final Navigator nav;
    private final String lessonId;

    public LessonView(Navigator nav, String lessonId) {
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

        Label title = new Label(lesson.getOrder() + ". " + lesson.getTitle());
        title.getStyleClass().add("h2");

        Label subtitle = new Label("Теория. После изучения перейдите к домашнему заданию.");
        subtitle.getStyleClass().add("muted");

        String md = nav.loader().readResourceText(lesson.getTheory().getMarkdown());
        String html = MarkdownRenderer.toHtml(md != null ? md : "");

        WebView web = new WebView();
        web.getEngine().loadContent(html);

        var back = AppButton.secondary("К списку уроков", e -> nav.showLessonList());
        var hw = AppButton.primary("Домашнее задание", e -> nav.showHomework(lessonId));

        HBox actions = new HBox(12, back, hw);
        actions.setAlignment(Pos.CENTER_RIGHT);

        BorderPane pane = new BorderPane();
        pane.setPadding(new Insets(18));
        pane.setTop(new VBoxHeader(title, subtitle).view());
        pane.setCenter(web);
        pane.setBottom(actions);
        BorderPane.setMargin(actions, new Insets(12, 0, 0, 0));
        return pane;
    }
}
