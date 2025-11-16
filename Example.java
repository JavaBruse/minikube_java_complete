import com.sun.net.httpserver.*;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

public class Example {
    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        // Тональность
        server.createContext("/text-tone", exchange -> {
            try {
                String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                String text = "no_text";

                if (requestBody.contains("\"text\"")) {
                    String[] parts = requestBody.split("\"text\"\\s*:\\s*\"");
                    if (parts.length > 1) {
                        text = parts[1].replaceAll("\".*", "");
                    }
                }

                String response = "{\"received_text\": \"" + text + "\", \"mood\": \"happy\", \"score\": 0.95}";
                sendResponse(exchange, response, "application/json");

            } catch (Exception e) {
                String response = "{\"error\": \"bad_request\", \"mood\": \"neutral\", \"score\": 0.5}";
                sendResponse(exchange, response, "application/json");
            }
        });

        // Health
        server.createContext("/health", exchange -> {
            String response = "{\"status\": \"OK\"}";
            sendResponse(exchange, response, "application/json");
        });

        server.setExecutor(null);
        server.start();

        // Metrics
        server.createContext("/metrics", exchange -> {
            long memoryUsed = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();

            String response =
                    "# HELP memory_usage_bytes Memory usage\n" +
                            "# TYPE memory_usage_bytes gauge\n" +
                            "memory_usage_bytes " + memoryUsed;

            exchange.getResponseHeaders().set("Content-Type", "text/plain");
            byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        });
    }

    private static void sendResponse(HttpExchange exchange, String response, String contentType) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", contentType);
        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(200, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }
}