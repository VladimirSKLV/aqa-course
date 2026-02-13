package ru.vlsklv.course.app.sandbox;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Минимальный sandbox-раннер для Java-кода:
 * - пишет исходник во временную папку
 * - компилирует через JavaCompiler (jdk.compiler)
 * - запускает main-класс отдельным процессом
 *
 * Примечание для EXE-сборки через jlink/jpackage:
 * в runtime-image должны присутствовать модули java.compiler и jdk.compiler,
 * иначе ToolProvider.getSystemJavaCompiler() вернёт null.
 */
public final class JavaSandboxRunner {
    public RunResult compileAndRun(String fileName, String mainClass, String source, Duration timeout) {
        if (fileName == null || fileName.isBlank()) fileName = "Main.java";
        if (mainClass == null || mainClass.isBlank()) mainClass = "Main";
        if (timeout == null) timeout = Duration.ofSeconds(5);

        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) {
            return RunResult.fail(
                    "В текущей сборке отсутствует Java-компилятор (ToolProvider.getSystemJavaCompiler() == null).\n" +
                            "Для запуска кодовых заданий в EXE/runtime-image нужно включить модули: java.compiler и jdk.compiler.\n" +
                            "Во время разработки запускайте приложение на JDK 17 (не JRE)."
            );
        }

        Path dir;
        try {
            dir = Files.createTempDirectory("aqa-course-sandbox-");
        } catch (IOException e) {
            return RunResult.fail("Не удалось создать временную директорию: " + e.getMessage());
        }

        try {
            Path src = dir.resolve(fileName);
            Files.writeString(src, source, StandardCharsets.UTF_8);

            DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
            try (StandardJavaFileManager fm = compiler.getStandardFileManager(diagnostics, null, StandardCharsets.UTF_8)) {
                Iterable<? extends JavaFileObject> units = fm.getJavaFileObjectsFromFiles(List.of(src.toFile()));
                List<String> options = List.of("--release", "17", "-encoding", "UTF-8", "-d", dir.toAbsolutePath().toString());

                Boolean ok = compiler.getTask(null, fm, diagnostics, options, null, units).call();
                if (ok == null || !ok) {
                    return RunResult.compileError(formatDiagnostics(diagnostics));
                }
            }

            String javaBin = resolveJavaBin();
            ProcessBuilder pb = new ProcessBuilder(
                    javaBin,
                    "-Xmx256m",
                    "-cp",
                    dir.toAbsolutePath().toString(),
                    mainClass
            );
            pb.redirectErrorStream(false);

            Process p = pb.start();
            boolean finished = p.waitFor(timeout.toMillis(), TimeUnit.MILLISECONDS);
            if (!finished) {
                p.destroyForcibly();
                return RunResult.runtimeError("Превышен таймаут выполнения (" + timeout.toSeconds() + "s). Процесс остановлен.");
            }

            String out = readAll(p.getInputStream());
            String err = readAll(p.getErrorStream());
            int code = p.exitValue();

            if (code != 0) {
                return RunResult.runtimeError(
                        "Процесс завершился с кодом " + code + ".\n" +
                                (err.isBlank() ? "" : ("stderr:\n" + err + "\n")) +
                                (out.isBlank() ? "" : ("stdout:\n" + out))
                );
            }

            return RunResult.ok(out, err);
        } catch (Exception e) {
            return RunResult.fail("Ошибка при компиляции/запуске: " + e.getMessage());
        } finally {
            try { deleteRecursive(dir); } catch (Exception ignored) {}
        }
    }

    private static String resolveJavaBin() {
        String javaHome = System.getProperty("java.home");
        if (javaHome == null || javaHome.isBlank()) return "java";
        Path bin = Path.of(javaHome, "bin", isWindows() ? "java.exe" : "java");
        return Files.exists(bin) ? bin.toAbsolutePath().toString() : "java";
    }

    private static boolean isWindows() {
        String os = System.getProperty("os.name", "").toLowerCase();
        return os.contains("win");
    }

    private static String readAll(InputStream is) throws IOException {
        if (is == null) return "";
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buf = new byte[8192];
        int r;
        while ((r = is.read(buf)) >= 0) {
            baos.write(buf, 0, r);
        }
        return baos.toString(StandardCharsets.UTF_8);
    }

    private static String formatDiagnostics(DiagnosticCollector<JavaFileObject> diagnostics) {
        StringBuilder sb = new StringBuilder();
        for (Diagnostic<? extends JavaFileObject> d : diagnostics.getDiagnostics()) {
            sb.append("[").append(d.getKind()).append("] ");
            sb.append("line ").append(d.getLineNumber()).append(": ");
            sb.append(d.getMessage(null)).append("\n");
        }
        return sb.toString();
    }

    private static void deleteRecursive(Path path) throws IOException {
        if (path == null || !Files.exists(path)) return;
        if (Files.isDirectory(path)) {
            try (var s = Files.list(path)) {
                for (Path p : s.toList()) deleteRecursive(p);
            }
        }
        Files.deleteIfExists(path);
    }

    public static final class RunResult {
        public enum Status { OK, COMPILE_ERROR, RUNTIME_ERROR, FAIL }

        private final Status status;
        private final String message;
        private final String stdout;
        private final String stderr;

        private RunResult(Status status, String message, String stdout, String stderr) {
            this.status = status;
            this.message = message;
            this.stdout = stdout;
            this.stderr = stderr;
        }

        public static RunResult ok(String stdout, String stderr) {
            return new RunResult(Status.OK, "", stdout == null ? "" : stdout, stderr == null ? "" : stderr);
        }

        public static RunResult compileError(String message) {
            return new RunResult(Status.COMPILE_ERROR, message == null ? "" : message, "", "");
        }

        public static RunResult runtimeError(String message) {
            return new RunResult(Status.RUNTIME_ERROR, message == null ? "" : message, "", "");
        }

        public static RunResult fail(String message) {
            return new RunResult(Status.FAIL, message == null ? "" : message, "", "");
        }

        public Status getStatus() { return status; }
        public String getMessage() { return message; }
        public String getStdout() { return stdout; }
        public String getStderr() { return stderr; }
    }
}
