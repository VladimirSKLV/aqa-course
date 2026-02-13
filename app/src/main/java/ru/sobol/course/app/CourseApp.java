package ru.sobol.course.app;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import ru.sobol.course.app.ui.Navigator;

public class CourseApp extends Application {
    @Override
    public void start(Stage stage) {
        Navigator nav = new Navigator(stage);

        Scene scene = new Scene(nav.root(), 1100, 720);
        scene.getStylesheets().add(
                getClass().getClassLoader().getResource("app.css").toExternalForm()
        );

        stage.setTitle("AQA Course");
        stage.setScene(scene);
        stage.show();

        nav.showWelcome();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
