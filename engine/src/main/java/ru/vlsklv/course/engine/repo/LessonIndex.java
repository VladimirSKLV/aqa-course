package ru.vlsklv.course.engine.repo;

import java.util.ArrayList;
import java.util.List;

public class LessonIndex {
    private List<LessonRef> lessons;
    private List<LessonBlock> blocks;

    public LessonIndex() {}

    public List<LessonRef> getLessons() { return lessons; }
    public void setLessons(List<LessonRef> lessons) { this.lessons = lessons; }

    public List<LessonBlock> getBlocks() { return blocks; }
    public void setBlocks(List<LessonBlock> blocks) { this.blocks = blocks; }

    public List<LessonRef> flattenLessonRefs() {
        List<LessonRef> out = new ArrayList<>();
        if (lessons != null) out.addAll(lessons);

        if (blocks != null) {
            for (LessonBlock block : blocks) {
                if (block == null || block.getLessons() == null) continue;
                out.addAll(block.getLessons());
            }
        }
        return out;
    }

    public static class LessonBlock {
        private String language;
        private String track;
        private List<LessonRef> lessons;

        public LessonBlock() {}

        public String getLanguage() { return language; }
        public void setLanguage(String language) { this.language = language; }

        public String getTrack() { return track; }
        public void setTrack(String track) { this.track = track; }

        public List<LessonRef> getLessons() { return lessons; }
        public void setLessons(List<LessonRef> lessons) { this.lessons = lessons; }
    }

    public static class LessonRef {
        private String path;

        public LessonRef() {}

        public String getPath() { return path; }
        public void setPath(String path) { this.path = path; }
    }
}
