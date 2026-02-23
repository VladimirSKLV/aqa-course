package ru.vlsklv.course.engine.repo;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import ru.vlsklv.course.engine.model.Lesson;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class LessonLoader {
    private final ObjectMapper yaml;

    public LessonLoader() {
        this.yaml = new ObjectMapper(new YAMLFactory())
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public LessonRepository loadFromClasspathIndex(String indexPath) {
        LessonIndex index = readYaml(indexPath, LessonIndex.class);
        if (index == null) {
            return new LessonRepository(List.of());
        }

        List<Lesson> lessons = new ArrayList<>();
        Set<String> seenPaths = new LinkedHashSet<>();

        for (LessonIndex.LessonRef ref : index.flattenLessonRefs()) {
            if (ref == null || ref.getPath() == null || ref.getPath().isBlank()) continue;
            if (!seenPaths.add(ref.getPath())) continue;
            Lesson lesson = readYaml(ref.getPath(), Lesson.class);
            if (lesson != null) lessons.add(lesson);
        }

        return new LessonRepository(lessons);
    }

    private <T> T readYaml(String classpathPath, Class<T> type) {
        String normalized = classpathPath.startsWith("/") ? classpathPath.substring(1) : classpathPath;
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(normalized)) {
            if (is == null) return null;
            return yaml.readValue(is, type);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to read YAML from classpath: " + classpathPath, e);
        }
    }

    public String readResourceText(String classpathPath) {
        String normalized = classpathPath.startsWith("/") ? classpathPath.substring(1) : classpathPath;
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(normalized)) {
            if (is == null) return null;
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to read resource: " + classpathPath, e);
        }
    }
}
