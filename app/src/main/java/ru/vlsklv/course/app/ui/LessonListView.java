package ru.vlsklv.course.app.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Accordion;
import javafx.scene.control.Label;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import ru.vlsklv.course.app.ui.kit.AppButton;
import ru.vlsklv.course.app.ui.kit.AppCard;
import ru.vlsklv.course.engine.model.CourseLanguage;
import ru.vlsklv.course.engine.model.CourseTrack;
import ru.vlsklv.course.engine.model.Lesson;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class LessonListView {
    private final Navigator nav;

    public LessonListView(Navigator nav) {
        this.nav = nav;
    }

    public Parent view() {
        CourseLanguage lang = nav.selectedLanguage();
        CourseTrack track = nav.selectedTrack();
        if (lang == null || track == null) {
            Label err = new Label("Не выбран язык или уровень курса.");
            err.getStyleClass().add("error");

            var back = AppButton.secondary("← На главный экран", e -> nav.showWelcome());

            var box = new VBox(12, err, back);
            box.setPadding(new Insets(18));
            return box;
        }

        List<Lesson> lessons = nav.lessonRepository().listByLanguageAndTrack(lang, track);

        String trackTitle = (track == CourseTrack.BEGINNER) ? "Начинающие" : "Продвинутые";
        Label title = new Label("📚 Программа курса: " + (lang == CourseLanguage.JAVA ? "Java" : "Kotlin") + " — " + trackTitle);
        title.getStyleClass().add("h2");

        if (lessons.isEmpty()) {
            Label empty = new Label("Пока нет уроков для этого направления.\nДобавьте уроки в content и обновите lessons/index.yml.");
            empty.getStyleClass().add("muted");
            empty.setWrapText(true);
            empty.setAlignment(Pos.CENTER);
            empty.setMaxWidth(Double.MAX_VALUE);

            var back = AppButton.secondary("← Назад", e -> nav.showTrackSelect());
            var sandbox = AppButton.secondary("🧪 Песочница", e -> nav.showSandbox());

            HBox actions = new HBox(12, back, sandbox);
            actions.setAlignment(Pos.CENTER);

            VBox inner = new VBox(14, title, empty, actions);
            inner.setAlignment(Pos.CENTER);
            inner.setPadding(new Insets(8));

            AppCard card = new AppCard(inner);
            card.setMaxWidth(820);

            BorderPane pane = new BorderPane();
            pane.setPadding(new Insets(28));
            pane.setCenter(card);
            return pane;
        }

        Label hint = new Label("Блоки и уроки раскрываются по клику. Открыть можно только доступные уроки по порядку.");
        hint.getStyleClass().add("muted");
        hint.setWrapText(true);

        Accordion blocksAccordion = new Accordion();
        blocksAccordion.getPanes().addAll(buildBlockPanes(lang, track, lessons));
        if (!blocksAccordion.getPanes().isEmpty()) {
            blocksAccordion.setExpandedPane(blocksAccordion.getPanes().get(0));
        }

        var back = AppButton.ghost("← Назад", e -> nav.showTrackSelect());

        var resume = AppButton.secondary("▶ Продолжить", e -> {
            String resumeId = nav.resolveResumeLessonId(lang, track);
            if (resumeId != null) nav.showLesson(resumeId);
        });
        resume.setDisable(nav.resolveResumeLessonId(lang, track) == null);

        HBox actions = new HBox(12, back, resume);
        actions.setAlignment(Pos.CENTER_RIGHT);

        BorderPane pane = new BorderPane();
        pane.setPadding(new Insets(18));
        pane.setTop(new VBoxHeader(title, hint).view());
        pane.setCenter(blocksAccordion);
        pane.setBottom(actions);
        BorderPane.setMargin(actions, new Insets(12, 0, 0, 0));
        return pane;
    }

    private List<TitledPane> buildBlockPanes(CourseLanguage lang, CourseTrack track, List<Lesson> lessons) {
        Map<String, VBox> grouped = new LinkedHashMap<>();
        for (Lesson lesson : lessons) {
            String blockName = blockForLesson(lang, track, lesson.getOrder());
            grouped.computeIfAbsent(blockName, k -> new VBox(10));
            grouped.get(blockName).getChildren().add(buildLessonPane(lessons, lesson));
        }

        return grouped.entrySet().stream()
                .map(e -> {
                    VBox content = e.getValue();
                    content.setPadding(new Insets(8, 6, 8, 6));
                    TitledPane pane = new TitledPane(e.getKey(), content);
                    pane.setAnimated(false);
                    return pane;
                })
                .toList();
    }

    private TitledPane buildLessonPane(List<Lesson> ordered, Lesson lesson) {
        boolean unlocked = isUnlocked(ordered, lesson);
        boolean done = nav.progress().isCompleted(lesson.getLanguage(), nav.selectedTrack(), lesson.getId());
        String status = done ? "✅" : (unlocked ? "▶" : "🔒");

        Label summary = new Label(summaryForLesson(lesson.getId()));
        summary.getStyleClass().add("muted");
        summary.setWrapText(true);

        AppButton open = AppButton.primary("Открыть урок", e -> nav.showLesson(lesson.getId()));
        open.setDisable(!unlocked && !done);

        VBox lessonBox = new VBox(8, summary, open);
        lessonBox.setPadding(new Insets(6, 0, 6, 0));

        TitledPane pane = new TitledPane(status + "  " + lesson.getOrder() + ". " + lesson.getTitle(), lessonBox);
        pane.setAnimated(false);
        pane.setCollapsible(true);
        return pane;
    }

    private String blockForLesson(CourseLanguage language, CourseTrack track, int order) {
        if (track == CourseTrack.BEGINNER) {
            return switch (order) {
                case 1 -> "Знакомство. Переменные";
                case 2 -> "Условия и ветвления";
                default -> "Циклы, методы и базовая автоматизация";
            };
        }

        if (language == CourseLanguage.JAVA) {
            return switch (order) {
                case 1 -> "Точность данных и время";
                case 2 -> "Типобезопасность и generics";
                default -> "Коллекции и функциональный стиль";
            };
        }

        return switch (order) {
            case 1 -> "Kotlin-модель данных для AQA";
            case 2 -> "Null-safety и выразительные условия";
            default -> "Коллекции, extension и DSL-подход";
        };
    }

    private String summaryForLesson(String lessonId) {
        return switch (lessonId) {
            case "java-001", "kotlin-001", "kotlin-adv-001" -> "Разберём типы данных, переменные и типичные ошибки хранения данных в автотестах.";
            case "java-002", "kotlin-002", "kotlin-adv-002" -> "Научимся строить условия и логические проверки так, чтобы тесты были читаемыми и предсказуемыми.";
            case "java-003", "kotlin-003", "java-adv-003", "kotlin-adv-003" -> "Практика: управлять потоком проверок, работать с коллекциями и писать переиспользуемые методы/функции.";
            case "java-adv-001" -> "Продвинутая работа с BigDecimal, временем и парсингом данных из API для борьбы с flaky-тестами.";
            case "java-adv-002" -> "Поймём generics и безопасные приведения типов, чтобы избежать ClassCastException в runtime.";
            default -> "Краткий обзор теории и практики урока с примерами, которые пригодятся в реальных AQA-задачах.";
        };
    }

    private boolean isUnlocked(List<Lesson> ordered, Lesson lesson) {
        for (Lesson l : ordered) {
            if (l.getOrder() >= lesson.getOrder()) break;
            if (!nav.progress().isCompleted(l.getLanguage(), nav.selectedTrack(), l.getId())) return false;
        }
        return true;
    }
}
