package ru.sobol.course.engine.progress;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import ru.sobol.course.engine.model.CourseLanguage;

import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Progress {
    private final Map<CourseLanguage, Set<String>> completedLessonIds;

    public Progress() {
        this.completedLessonIds = new EnumMap<>(CourseLanguage.class);
        for (CourseLanguage lang : CourseLanguage.values()) {
            completedLessonIds.put(lang, new HashSet<>());
        }
    }

    @JsonCreator
    public Progress(@JsonProperty("completedLessonIds") Map<CourseLanguage, Set<String>> completedLessonIds) {
        this.completedLessonIds = new EnumMap<>(CourseLanguage.class);
        for (CourseLanguage lang : CourseLanguage.values()) {
            Set<String> v = completedLessonIds != null ? completedLessonIds.get(lang) : null;
            this.completedLessonIds.put(lang, v != null ? new HashSet<>(v) : new HashSet<>());
        }
    }

    public Map<CourseLanguage, Set<String>> getCompletedLessonIds() {
        Map<CourseLanguage, Set<String>> ro = new EnumMap<>(CourseLanguage.class);
        completedLessonIds.forEach((k, v) -> ro.put(k, Collections.unmodifiableSet(v)));
        return Collections.unmodifiableMap(ro);
    }

    public boolean isCompleted(CourseLanguage language, String lessonId) {
        return completedLessonIds.getOrDefault(language, Set.of()).contains(lessonId);
    }

    public void markCompleted(CourseLanguage language, String lessonId) {
        completedLessonIds.computeIfAbsent(language, k -> new HashSet<>()).add(lessonId);
    }
}
