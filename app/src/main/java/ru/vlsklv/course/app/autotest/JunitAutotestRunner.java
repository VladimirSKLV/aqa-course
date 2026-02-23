package ru.vlsklv.course.app.autotest;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class JunitAutotestRunner {
    public enum SuiteType { API, WEB }

    public RunSummary runSuite(SuiteType suiteType, String baseUrl) {
        String normalizedBaseUrl = normalizeBaseUrl(baseUrl);
        if (normalizedBaseUrl == null) {
            return RunSummary.fail("BaseUrl пустой. Укажите URL тренажёра, например https://jsonplaceholder.typicode.com");
        }

        List<TestCase> cases = suiteType == SuiteType.API
                ? apiCases(normalizedBaseUrl)
                : webCases(normalizedBaseUrl);

        int passed = 0;
        List<String> failures = new ArrayList<>();
        for (TestCase tc : cases) {
            try {
                tc.execute();
                passed++;
            } catch (Exception ex) {
                failures.add("- " + tc.name + ": " + ex.getMessage());
            }
        }

        int total = cases.size();
        int failed = total - passed;
        StringBuilder sb = new StringBuilder();
        sb.append("Suite: ").append(suiteType).append("\n")
                .append("baseUrl: ").append(normalizedBaseUrl).append("\n")
                .append("Всего: ").append(total).append(" | passed: ").append(passed).append(" | failed: ").append(failed).append("\n");
        if (!failures.isEmpty()) {
            sb.append("\nПадения:\n");
            failures.forEach(f -> sb.append(f).append("\n"));
        }

        return new RunSummary(failed == 0, sb.toString());
    }

    public ProbeResult sendProbe(String baseUrl, String endpoint) {
        String normalizedBaseUrl = normalizeBaseUrl(baseUrl);
        if (normalizedBaseUrl == null) {
            return ProbeResult.fail("BaseUrl пустой. Укажите URL тренажёра.");
        }

        String safeEndpoint = endpoint == null || endpoint.isBlank() ? "/" : endpoint.trim();
        String fullUrl = buildUrl(normalizedBaseUrl, safeEndpoint);

        long started = System.nanoTime();
        try {
            HttpResponse<String> response = get(fullUrl);
            long elapsedMs = (System.nanoTime() - started) / 1_000_000;
            String body = response.body() == null ? "" : response.body();
            if (body.length() > 1200) body = body.substring(0, 1200) + "\n...<truncated>";
            return ProbeResult.ok(fullUrl, response.statusCode(), elapsedMs, body);
        } catch (Exception e) {
            return ProbeResult.fail("Ошибка запроса: " + e.getMessage());
        }
    }

    private List<TestCase> apiCases(String baseUrl) {
        return List.of(
                new TestCase("GET /posts/1 возвращает 2xx", () -> {
                    HttpResponse<String> response = get(baseUrl + "/posts/1");
                    assertTrue(response.statusCode() >= 200 && response.statusCode() < 300, "HTTP код должен быть 2xx");
                    assertTrue(response.body().contains("id"), "В ответе ожидается поле id");
                }),
                new TestCase("Негативный endpoint возвращает 4xx/5xx", () -> {
                    HttpResponse<String> response = get(baseUrl + "/this-endpoint-should-not-exist");
                    assertTrue(response.statusCode() >= 400, "Негативный кейс должен вернуть 4xx/5xx");
                })
        );
    }

    private List<TestCase> webCases(String baseUrl) {
        return List.of(
                new TestCase("Главная страница доступна", () -> {
                    HttpResponse<String> response = get(baseUrl + "/");
                    assertTrue(response.statusCode() >= 200 && response.statusCode() < 400, "Главная страница должна быть доступна");
                }),
                new TestCase("Главная страница возвращает HTML/JSON", () -> {
                    HttpResponse<String> response = get(baseUrl + "/");
                    String body = response.body() == null ? "" : response.body().toLowerCase();
                    assertTrue(body.contains("<html") || body.contains("{") || body.contains("["),
                            "Ответ должен быть HTML или JSON для smoke-проверки");
                })
        );
    }

    private static void assertTrue(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }

    private static String normalizeBaseUrl(String baseUrl) {
        if (baseUrl == null || baseUrl.isBlank()) return null;
        String normalized = baseUrl.trim();
        if (normalized.endsWith("/")) normalized = normalized.substring(0, normalized.length() - 1);
        return normalized;
    }

    private static String buildUrl(String baseUrl, String endpoint) {
        if (endpoint.startsWith("http://") || endpoint.startsWith("https://")) return endpoint;
        if (!endpoint.startsWith("/")) endpoint = "/" + endpoint;
        return baseUrl + endpoint;
    }

    private static HttpResponse<String> get(String fullUrl) throws IOException, InterruptedException {
        HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(8)).build();
        HttpRequest request = HttpRequest.newBuilder(URI.create(fullUrl))
                .timeout(Duration.ofSeconds(15))
                .GET()
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private record TestCase(String name, CheckedRunnable execute) {}
    @FunctionalInterface
    private interface CheckedRunnable { void run() throws Exception; }

    public record RunSummary(boolean success, String details) {
        public static RunSummary fail(String message) { return new RunSummary(false, message); }
    }

    public record ProbeResult(boolean success, String url, int statusCode, long elapsedMs, String body, String error) {
        public static ProbeResult ok(String url, int statusCode, long elapsedMs, String body) {
            return new ProbeResult(true, url, statusCode, elapsedMs, body, "");
        }
        public static ProbeResult fail(String error) { return new ProbeResult(false, "", 0, 0, "", error); }
    }
}
