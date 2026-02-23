import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

public class Main {
    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/user/1", exchange -> respond(exchange, 200, "{\"id\":1,\"name\":\"QA\"}"));
        server.createContext("/user/404", exchange -> respond(exchange, 404, "{\"error\":\"USER_NOT_FOUND\"}"));
        server.start();

        String baseUrl = "http://127.0.0.1:" + server.getAddress().getPort();
        HttpClient client = HttpClient.newHttpClient();

        HttpResponse<String> ok = get(client, baseUrl + "/user/1");
        HttpResponse<String> nf = get(client, baseUrl + "/user/404");

        assertThat(ok.statusCode() == 200, "Ожидали 200");
        assertThat(ok.body().contains("\"name\":\"QA\""), "Ожидали name=QA");

        assertThat(nf.statusCode() == 404, "Ожидали 404");
        assertThat(nf.body().contains("USER_NOT_FOUND"), "Ожидали код ошибки USER_NOT_FOUND");

        System.out.println("ASSERT_OK_STATUS=PASS");
        System.out.println("ASSERT_OK_BODY=PASS");
        System.out.println("ASSERT_404_STATUS=PASS");
        System.out.println("ASSERT_404_BODY=PASS");

        server.stop(0);
    }

    static HttpResponse<String> get(HttpClient client, String url) throws Exception {
        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                .timeout(Duration.ofSeconds(5))
                .GET().build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    static void assertThat(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }

    static void respond(com.sun.net.httpserver.HttpExchange exchange, int code, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(code, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
        exchange.close();
    }
}
