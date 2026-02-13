package ru.sobol.course.app.ui;

import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import ru.sobol.course.engine.model.CourseLanguage;
import ru.sobol.course.engine.progress.Progress;
import ru.sobol.course.engine.progress.ProgressStore;
import ru.sobol.course.engine.repo.LessonLoader;
import ru.sobol.course.engine.repo.LessonRepository;

import java.nio.file.Path;

public class Navigator {
    private final Stage stage;
    private final BorderPane root;

    private final LessonLoader loader;
    private final LessonRepository lessons;

    private final ProgressStore progressStore;
    private final Path progressPath;
    private Progress progress;

    private CourseLanguage selectedLanguage;

    public Navigator(Stage stage) {
        this.stage = stage;
        this.root = new BorderPane();

        this.loader = new LessonLoader();
        this.lessons = loader.loadFromClasspathIndex("lessons/index.yml");

        this.progressStore = new ProgressStore();
        this.progressPath = progressStore.defaultPath();
        this.progress = progressStore.loadOrEmpty(progressPath);
    }

    public Parent root() {
        return root;
    }

    public LessonRepository lessonRepository() {
        return lessons;
    }

    public LessonLoader loader() {
        return loader;
    }

    public Progress progress() {
        return progress;
    }

    public void saveProgress() {
        progressStore.save(progressPath, progress);
    }

    public void selectLanguage(CourseLanguage language) {
        this.selectedLanguage = language;
    }

    public CourseLanguage selectedLanguage() {
        return selectedLanguage;
    }

    public void showWelcome() {
        root.setCenter(new WelcomeView(this).view());
    }

    public void showLessonList() {
        root.setCenter(new LessonListView(this).view());
    }

    public void showLesson(String lessonId) {
        root.setCenter(new LessonView(this, lessonId).view());
    }

    public void showQuiz(String lessonId) {
        root.setCenter(new QuizView(this, lessonId).view());
    }
}
