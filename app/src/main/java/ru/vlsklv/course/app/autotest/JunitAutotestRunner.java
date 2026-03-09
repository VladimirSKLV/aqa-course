package ru.vlsklv.course.app.autotest;

import java.awt.Desktop;
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

    public List<ApiTarget> apiTargets() {
        return List.of(
                new ApiTarget(
                        "jsonplaceholder",
                        "JSONPlaceholder",
                        "https://jsonplaceholder.typicode.com",
                        "/posts/1",
                        "Публичный REST-тренажёр с постами/комментариями"
                ),
                new ApiTarget(
                        "reqres",
                        "ReqRes",
                        "https://reqres.in",
                        "/api/users/2",
                        "Тренажёр пользовательских эндпоинтов и негативных ответов"
                ),
                new ApiTarget(
                        "httpbin",
                        "HttpBin",
                        "https://httpbin.org",
                        "/get",
                        "Сервис для отладки HTTP-запросов и заголовков"
                )
        );
    }

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

        if (suiteType == SuiteType.WEB) {
            sb.append("Открытие браузера: ").append(tryOpenBrowser(normalizedBaseUrl)).append("\n");
        }

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
        if (baseUrl.contains("jsonplaceholder.typicode.com")) {
            return List.of(
                    new TestCase("GET /posts/1 возвращает id=1", () -> {
                        HttpResponse<String> response = get(baseUrl + "/posts/1");
                        assertTrue(response.statusCode() == 200, "Ожидается HTTP 200");
                        assertTrue(response.body().contains("\"id\": 1") || response.body().contains("\"id\":1"), "Ожидается id=1");
                    }),
                    new TestCase("GET /users возвращает массив пользователей", () -> {
                        HttpResponse<String> response = get(baseUrl + "/users");
                        assertTrue(response.statusCode() == 200, "Ожидается HTTP 200");
                        assertTrue(response.body().startsWith("["), "Ожидается JSON-массив");
                    }),
                    new TestCase("Негативный endpoint возвращает 4xx/5xx", () -> {
                        HttpResponse<String> response = get(baseUrl + "/this-endpoint-should-not-exist");
                        assertTrue(response.statusCode() >= 400, "Негативный кейс должен вернуть 4xx/5xx");
                    })
            );
        }

        if (baseUrl.contains("reqres.in")) {
            return List.of(
                    new TestCase("GET /api/users/2 возвращает 200 и data", () -> {
                        HttpResponse<String> response = get(baseUrl + "/api/users/2");
                        assertTrue(response.statusCode() == 200, "Ожидается HTTP 200");
                        assertTrue(response.body().contains("\"data\""), "Ожидается объект data");
                    }),
                    new TestCase("GET /api/users/23 возвращает 404", () -> {
                        HttpResponse<String> response = get(baseUrl + "/api/users/23");
                        assertTrue(response.statusCode() == 404, "Ожидается HTTP 404");
                    }),
                    new TestCase("GET /api/unknown возвращает список ресурсов", () -> {
                        HttpResponse<String> response = get(baseUrl + "/api/unknown");
                        assertTrue(response.statusCode() == 200, "Ожидается HTTP 200");
                        assertTrue(response.body().contains("\"data\""), "Ожидается поле data");
                    })
            );
        }

        if (baseUrl.contains("httpbin.org")) {
            return List.of(
                    new TestCase("GET /get возвращает 200", () -> {
                        HttpResponse<String> response = get(baseUrl + "/get");
                        assertTrue(response.statusCode() == 200, "Ожидается HTTP 200");
                        assertTrue(response.body().contains("\"url\""), "Ожидается поле url");
                    }),
                    new TestCase("GET /status/418 возвращает 418", () -> {
                        HttpResponse<String> response = get(baseUrl + "/status/418");
                        assertTrue(response.statusCode() == 418, "Ожидается HTTP 418");
                    }),
                    new TestCase("GET /json возвращает JSON", () -> {
                        HttpResponse<String> response = get(baseUrl + "/json");
                        assertTrue(response.statusCode() == 200, "Ожидается HTTP 200");
                        assertTrue(response.body().contains("slideshow"), "Ожидается JSON-структура slideshow");
                    })
            );
        }

        return List.of(
                new TestCase("GET / должен вернуть 2xx/3xx", () -> {
                    HttpResponse<String> response = get(baseUrl + "/");
                    assertTrue(response.statusCode() >= 200 && response.statusCode() < 400, "Ожидается HTTP 2xx/3xx");
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

    private static String tryOpenBrowser(String baseUrl) {
        try {
            if (!Desktop.isDesktopSupported()) {
                return "не поддерживается в текущем окружении";
            }
            Desktop.getDesktop().browse(URI.create(baseUrl));
            return "успешно";
        } catch (Exception e) {
            return "ошибка: " + e.getMessage();
        }
    }

    private record TestCase(String name, CheckedRunnable execute) {}
    @FunctionalInterface
    private interface CheckedRunnable { void run() throws Exception; }

    public record ApiTarget(String id, String title, String baseUrl, String defaultEndpoint, String description) {
        @Override
        public String toString() {
            return title + " — " + baseUrl;
        }
    }

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
