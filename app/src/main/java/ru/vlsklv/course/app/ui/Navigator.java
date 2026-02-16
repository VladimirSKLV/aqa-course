package ru.vlsklv.course.app.ui;

import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import ru.vlsklv.course.app.ui.kit.Fx;
import ru.vlsklv.course.engine.model.*;
import ru.vlsklv.course.engine.progress.Progress;
import ru.vlsklv.course.engine.progress.ProgressStore;
import ru.vlsklv.course.engine.repo.LessonLoader;
import ru.vlsklv.course.engine.repo.LessonRepository;

import java.nio.file.Path;
import java.util.List;

public class Navigator {
    private final Stage stage;
    private final BorderPane root;

    private final LessonLoader loader;
    private final LessonRepository lessons;

    private final ProgressStore progressStore;
    private final Path progressPath;
    private final Progress progress;

    private CourseLanguage selectedLanguage;
    private CourseTrack selectedTrack;

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

    public void selectTrack(CourseTrack track) {
        this.selectedTrack = track;
    }

    public CourseLanguage selectedLanguage() {
        return selectedLanguage;
    }

    public CourseTrack selectedTrack() {
        return selectedTrack;
    }

    public boolean hasResume() {
        return progress.hasAnyProgress();
    }

    public void resumeLast() {
        Progress.CourseKey key = progress.findMostRecentCourseKey();
        if (key == null) {
            showWelcome();
            return;
        }
        CourseLanguage lang = key.language();
        CourseTrack track = key.track();
        selectLanguage(lang);
        selectTrack(track);
        String lessonId = resolveResumeLessonId(lang, track);
        if (lessonId == null) showTrackSelect();
        else showLesson(lessonId);
    }

    public String resolveResumeLessonId(CourseLanguage lang, CourseTrack track) {
        Progress.CourseProgress cp = progress.course(lang, track);

        if (cp.getLastOpenedLessonId() != null) {
            Lesson l = lessons.findById(cp.getLastOpenedLessonId());
            if (l != null) return l.getId();
        }

        var ordered = lessons.listByLanguageAndTrack(lang, track);
        if (ordered.isEmpty()) return null;

        for (Lesson l : ordered) {
            if (!progress.isCompleted(lang, track, l.getId())) {
                if (isUnlocked(ordered, lang, track, l)) return l.getId();
                break;
            }
        }

        return ordered.get(ordered.size() - 1).getId();
    }

    public boolean isUnlocked(List<Lesson> ordered, CourseLanguage lang, CourseTrack track, Lesson lesson) {
        for (Lesson l : ordered) {
            if (l.getOrder() >= lesson.getOrder()) break;
            if (!progress.isCompleted(lang, track, l.getId())) return false;
        }
        return true;
    }

    private void setCenter(Parent view) {
        root.setCenter(view);
        Fx.enhance(view);
        Fx.pageEnter(view);
    }

    public void showWelcome() {
        setCenter(new WelcomeView(this).view());
    }

    public void showTrackSelect() {
        setCenter(new TrackSelectView(this).view());
    }

    public void showLessonList() {
        setCenter(new LessonListView(this).view());
    }

    public void showLesson(String lessonId) {
        if (selectedLanguage != null && selectedTrack != null && lessonId != null) {
            progress.setLastOpened(selectedLanguage, selectedTrack, lessonId, System.currentTimeMillis());
            saveProgress();
        }
        setCenter(new LessonView(this, lessonId).view());
    }

    public void showQuiz(String lessonId) {
        if (selectedLanguage != null && selectedTrack != null && lessonId != null) {
            progress.setLastOpened(selectedLanguage, selectedTrack, lessonId, System.currentTimeMillis());
            saveProgress();
        }
        setCenter(new QuizView(this, lessonId).view());
    }

    public void showCodeAssignment(String lessonId) {
        if (selectedLanguage != null && selectedTrack != null && lessonId != null) {
            progress.setLastOpened(selectedLanguage, selectedTrack, lessonId, System.currentTimeMillis());
            saveProgress();
        }
        setCenter(new CodeAssignmentView(this, lessonId).view());
    }

    /**
     * Открыть домашнее задание в зависимости от типа (quiz/code).
     */
    public void showHomework(String lessonId) {
        Lesson lesson = lessons.findById(lessonId);
        if (lesson == null) {
            showQuiz(lessonId);
            return;
        }

        if (lesson.getAssignment() instanceof QuizAssignment) {
            showQuiz(lessonId);
            return;
        }
        if (lesson.getAssignment() instanceof CodeAssignment) {
            showCodeAssignment(lessonId);
            return;
        }

        if (lesson.getAssignment() instanceof CompositeAssignment) {
            root.setCenter(new CompositeHomeworkView(this, lessonId).view());
            return;
        }

        showQuiz(lessonId);
    }

    public void showSandbox() {
        setCenter(new SandboxView(this).view());
    }
}
