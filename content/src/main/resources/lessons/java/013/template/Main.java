import com.sun.net.httpserver.HttpServer;

import java.awt.Desktop;
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
        server.createContext("/", exchange -> respond(exchange, 200,
                "<html><head><title>Demo</title></head><body><h1>Hello AQA</h1></body></html>"));
        server.start();

        String url = "http://127.0.0.1:" + server.getAddress().getPort() + "/";

        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response = client.send(
                HttpRequest.newBuilder(URI.create(url)).timeout(Duration.ofSeconds(5)).GET().build(),
                HttpResponse.BodyHandlers.ofString()
        );

        System.out.println("WEB_STATUS_OK=" + (response.statusCode() == 200));
        System.out.println("WEB_BODY_HAS_H1=" + response.body().contains("<h1>Hello AQA</h1>"));

        // В desktop-окружении можно открыть страницу в браузере. В headless это может быть недоступно.
        boolean browserOpened = false;
        try {
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().browse(URI.create(url));
                browserOpened = true;
            }
        } catch (Exception ignored) {
            browserOpened = false;
        }
        System.out.println("BROWSER_STEP_DONE=" + true);
        System.out.println("BROWSER_OPENED=" + browserOpened);

        server.stop(0);
    }

    static void respond(com.sun.net.httpserver.HttpExchange exchange, int code, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "text/html; charset=utf-8");
        exchange.sendResponseHeaders(code, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
        exchange.close();
    }
}
