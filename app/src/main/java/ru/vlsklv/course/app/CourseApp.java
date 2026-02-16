package ru.vlsklv.course.app;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import ru.vlsklv.course.app.ui.Navigator;

import java.util.Objects;

public class CourseApp extends Application {
    @Override
    public void start(Stage stage) {
        Navigator nav = new Navigator(stage);

        Scene scene = new Scene(nav.root(), 1200, 800);
        scene.getStylesheets().add(
                Objects.requireNonNull(getClass().getClassLoader().getResource("app.css")).toExternalForm()
        );

        scene.getRoot().getStyleClass().add("app-root");

        stage.setTitle("AQA Course");
        stage.setScene(scene);

        stage.setResizable(true);
        stage.setMinWidth(980);
        stage.setMinHeight(680);

        stage.show();
        nav.showWelcome();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
