package ru.sobol.course.engine.progress;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.nio.file.Files;
import java.nio.file.Path;

public class ProgressStore {
    private final ObjectMapper json;

    public ProgressStore() {
        this.json = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
    }

    public Path defaultPath() {
        String home = System.getProperty("user.home");
        return Path.of(home, ".aqa-course", "progress.json");
    }

    public Progress loadOrEmpty(Path path) {
        try {
            if (!Files.exists(path)) return new Progress();
            byte[] bytes = Files.readAllBytes(path);
            if (bytes.length == 0) return new Progress();
            return json.readValue(bytes, Progress.class);
        } catch (Exception e) {
            // Если файл повреждён — безопаснее начать с пустого прогресса
            return new Progress();
        }
    }

    public void save(Path path, Progress progress) {
        try {
            Files.createDirectories(path.getParent());
            Files.write(path, json.writeValueAsBytes(progress));
        } catch (Exception e) {
            throw new IllegalStateException("Failed to save progress to " + path, e);
        }
    }
}
