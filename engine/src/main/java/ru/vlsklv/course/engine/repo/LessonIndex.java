package ru.vlsklv.course.engine.repo;

import java.util.List;

public class LessonIndex {
    private List<LessonRef> lessons;

    public LessonIndex() {}

    public List<LessonRef> getLessons() { return lessons; }
    public void setLessons(List<LessonRef> lessons) { this.lessons = lessons; }

    public static class LessonRef {
        private String path;

        public LessonRef() {}

        public String getPath() { return path; }
        public void setPath(String path) { this.path = path; }
    }
}
