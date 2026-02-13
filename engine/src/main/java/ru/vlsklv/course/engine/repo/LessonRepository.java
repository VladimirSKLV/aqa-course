package ru.vlsklv.course.engine.repo;

import ru.vlsklv.course.engine.model.CourseLanguage;
import ru.vlsklv.course.engine.model.CourseTrack;
import ru.vlsklv.course.engine.model.Lesson;

import java.util.Comparator;
import java.util.List;

public class LessonRepository {
    private final List<Lesson> allLessons;

    public LessonRepository(List<Lesson> allLessons) {
        this.allLessons = allLessons.stream()
                .sorted(Comparator.comparing(Lesson::getLanguage)
                        .thenComparing(Lesson::getTrack, Comparator.nullsFirst(Comparator.naturalOrder()))
                        .thenComparingInt(Lesson::getOrder))
                .toList();
    }

    public List<Lesson> listByLanguage(CourseLanguage language) {
        // Сохранено для обратной совместимости: если уроки не размечены track — вернётся общий список.
        return allLessons.stream()
                .filter(l -> l.getLanguage() == language)
                .sorted(Comparator.comparing(Lesson::getTrack, Comparator.nullsFirst(Comparator.naturalOrder()))
                        .thenComparingInt(Lesson::getOrder))
                .toList();
    }

    public List<Lesson> listByLanguageAndTrack(CourseLanguage language, CourseTrack track) {
        return allLessons.stream()
                .filter(l -> l.getLanguage() == language)
                .filter(l -> l.getTrack() == track)
                .sorted(Comparator.comparingInt(Lesson::getOrder))
                .toList();
    }

    public Lesson findById(String lessonId) {
        return allLessons.stream()
                .filter(l -> l.getId().equals(lessonId))
                .findFirst()
                .orElse(null);
    }
}
