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

    // TODO: напишите простой smoke check: вернуть true, если status в диапазоне 200..299
    static boolean is2xx(int status) {
        return status >= 200 && status < 300;
    }

    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/ok", exchange -> respond(exchange, 200, "{\"status\":\"OK\"}"));
        server.createContext("/missing", exchange -> respond(exchange, 404, "{\"error\":\"NOT_FOUND\"}"));
        server.start();

        int port = server.getAddress().getPort();
        String baseUrl = "http://127.0.0.1:" + port;

        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(3))
                .build();

        HttpResponse<String> ok = sendGet(client, baseUrl + "/ok");
        HttpResponse<String> missing = sendGet(client, baseUrl + "/missing");

        System.out.println("OK_STATUS=" + ok.statusCode());
        System.out.println("OK_IS_2XX=" + is2xx(ok.statusCode()));
        System.out.println("OK_BODY_HAS_STATUS=" + ok.body().contains("\"status\":\"OK\""));

        System.out.println("MISSING_STATUS=" + missing.statusCode());
        System.out.println("MISSING_IS_2XX=" + is2xx(missing.statusCode()));
        System.out.println("MISSING_BODY_HAS_ERROR=" + missing.body().contains("NOT_FOUND"));

        server.stop(0);
    }

    static HttpResponse<String> sendGet(HttpClient client, String url) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                .GET()
                .timeout(Duration.ofSeconds(5))
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
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
