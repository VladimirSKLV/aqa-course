package ru.sobol.course.engine.repo;

import ru.sobol.course.engine.model.CourseLanguage;
import ru.sobol.course.engine.model.Lesson;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class LessonRepository {
    private final List<Lesson> allLessons;

    public LessonRepository(List<Lesson> allLessons) {
        this.allLessons = allLessons.stream()
                .sorted(Comparator.comparing(Lesson::getLanguage).thenComparingInt(Lesson::getOrder))
                .collect(Collectors.toUnmodifiableList());
    }

    public List<Lesson> listByLanguage(CourseLanguage language) {
        return allLessons.stream()
                .filter(l -> l.getLanguage() == language)
                .sorted(Comparator.comparingInt(Lesson::getOrder))
                .collect(Collectors.toUnmodifiableList());
    }

    public Lesson findById(String lessonId) {
        return allLessons.stream()
                .filter(l -> l.getId().equals(lessonId))
                .findFirst()
                .orElse(null);
    }
}
