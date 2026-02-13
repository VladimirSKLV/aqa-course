package ru.vlsklv.course.engine.progress;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import ru.vlsklv.course.engine.model.CourseLanguage;
import ru.vlsklv.course.engine.model.CourseTrack;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Локальный прогресс пользователя.
 *
 * Прогресс ведётся отдельно для каждой ветки (language + track).
 *
 * Формат backward-compatible: если в старом progress.json был только completedLessonIds по языку,
 * он будет импортирован в ветку BEGINNER.
 */
public class Progress {
    private final Map<String, CourseProgress> courses;

    @JsonCreator
    public Progress(
            @JsonProperty("courses") Map<String, CourseProgress> courses,
            // legacy (v1): Map<LANG, Set<lessonId>>
            @JsonProperty("completedLessonIds") Map<CourseLanguage, Set<String>> legacyCompletedLessonIds
    ) {
        this.courses = new HashMap<>();

        if (courses != null) {
            for (Map.Entry<String, CourseProgress> e : courses.entrySet()) {
                if (e.getKey() == null) continue;
                CourseProgress cp = e.getValue() != null ? e.getValue().normalized() : new CourseProgress();
                this.courses.put(e.getKey(), cp);
            }
        }

        // Импорт legacy-прогресса (если новый отсутствует)
        if (this.courses.isEmpty() && legacyCompletedLessonIds != null) {
            for (Map.Entry<CourseLanguage, Set<String>> e : legacyCompletedLessonIds.entrySet()) {
                CourseLanguage lang = e.getKey();
                if (lang == null) continue;
                CourseProgress cp = course(lang, CourseTrack.BEGINNER);
                Set<String> ids = e.getValue();
                if (ids != null) cp.completedLessonIds.addAll(ids);
            }
        }
    }

    public Progress() {
        this.courses = new HashMap<>();
    }

    public Map<String, CourseProgress> getCourses() {
        Map<String, CourseProgress> ro = new HashMap<>();
        for (Map.Entry<String, CourseProgress> e : courses.entrySet()) {
            ro.put(e.getKey(), e.getValue().immutableView());
        }
        return Collections.unmodifiableMap(ro);
    }

    public CourseProgress course(CourseLanguage language, CourseTrack track) {
        String key = CourseKey.of(language, track).asString();
        return courses.computeIfAbsent(key, k -> new CourseProgress());
    }

    public boolean isCompleted(CourseLanguage language, CourseTrack track, String lessonId) {
        if (lessonId == null) return false;
        return course(language, track).completedLessonIds.contains(lessonId);
    }

    public void markCompleted(CourseLanguage language, CourseTrack track, String lessonId) {
        if (lessonId == null) return;
        course(language, track).completedLessonIds.add(lessonId);
    }

    public void setLastOpened(CourseLanguage language, CourseTrack track, String lessonId, long openedAtEpochMs) {
        CourseProgress cp = course(language, track);
        cp.lastOpenedLessonId = lessonId;
        cp.lastOpenedAtEpochMs = openedAtEpochMs;
    }

    public CourseProgress findMostRecentlyOpened() {
        CourseProgress best = null;
        for (CourseProgress cp : courses.values()) {
            if (cp == null || cp.lastOpenedLessonId == null) continue;
            if (best == null || cp.lastOpenedAtEpochMs > best.lastOpenedAtEpochMs) best = cp;
        }
        return best;
    }

    public CourseKey findMostRecentCourseKey() {
        String bestKey = null;
        long bestTs = Long.MIN_VALUE;
        for (Map.Entry<String, CourseProgress> e : courses.entrySet()) {
            CourseProgress cp = e.getValue();
            if (cp == null || cp.lastOpenedLessonId == null) continue;
            if (cp.lastOpenedAtEpochMs > bestTs) {
                bestTs = cp.lastOpenedAtEpochMs;
                bestKey = e.getKey();
            }
        }
        return bestKey != null ? CourseKey.parse(bestKey) : null;
    }

    public boolean hasAnyProgress() {
        for (CourseProgress cp : courses.values()) {
            if (cp == null) continue;
            if (!cp.completedLessonIds.isEmpty() || cp.lastOpenedLessonId != null) return true;
        }
        return false;
    }

    public static final class CourseKey {
        private final CourseLanguage language;
        private final CourseTrack track;

        private CourseKey(CourseLanguage language, CourseTrack track) {
            this.language = Objects.requireNonNull(language, "language");
            this.track = Objects.requireNonNull(track, "track");
        }

        public static CourseKey of(CourseLanguage language, CourseTrack track) {
            return new CourseKey(language, track);
        }

        public static CourseKey parse(String s) {
            if (s == null || s.isBlank()) return null;
            String[] parts = s.split(":", 2);
            if (parts.length != 2) return null;
            try {
                return new CourseKey(CourseLanguage.valueOf(parts[0]), CourseTrack.valueOf(parts[1]));
            } catch (Exception e) {
                return null;
            }
        }

        public CourseLanguage language() { return language; }
        public CourseTrack track() { return track; }

        public String asString() {
            return language.name() + ":" + track.name();
        }
    }

    public static class CourseProgress {
        private Set<String> completedLessonIds = new HashSet<>();
        private String lastOpenedLessonId;
        private long lastOpenedAtEpochMs;

        public CourseProgress() {}

        public Set<String> getCompletedLessonIds() {
            return completedLessonIds;
        }

        public void setCompletedLessonIds(Set<String> completedLessonIds) {
            this.completedLessonIds = completedLessonIds != null ? new HashSet<>(completedLessonIds) : new HashSet<>();
        }

        public String getLastOpenedLessonId() {
            return lastOpenedLessonId;
        }

        public void setLastOpenedLessonId(String lastOpenedLessonId) {
            this.lastOpenedLessonId = lastOpenedLessonId;
        }

        public long getLastOpenedAtEpochMs() {
            return lastOpenedAtEpochMs;
        }

        public void setLastOpenedAtEpochMs(long lastOpenedAtEpochMs) {
            this.lastOpenedAtEpochMs = lastOpenedAtEpochMs;
        }

        private CourseProgress normalized() {
            if (completedLessonIds == null) completedLessonIds = new HashSet<>();
            return this;
        }

        private CourseProgress immutableView() {
            CourseProgress cp = new CourseProgress();
            cp.completedLessonIds = Collections.unmodifiableSet(new HashSet<>(this.completedLessonIds));
            cp.lastOpenedLessonId = this.lastOpenedLessonId;
            cp.lastOpenedAtEpochMs = this.lastOpenedAtEpochMs;
            return cp;
        }
    }
}
