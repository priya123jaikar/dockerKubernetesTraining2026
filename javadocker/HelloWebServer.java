import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Paths;

public class HelloWebServer {
    public static void main(String[] args) throws IOException {
        int port = 8080;
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/", new RootHandler());
        server.setExecutor(null);
        System.out.println("Starting server on http://localhost:" + port);
        server.start();
    }

    private static class RootHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String response;
            
            // Try to read index.html from the working directory
            try {
                byte[] fileContent = Files.readAllBytes(Paths.get("index.html"));
                response = new String(fileContent, "UTF-8");
            } catch (IOException e) {
                // Fallback if index.html not found
                response = "<html><head><title>Java Docker Web</title></head>"
                        + "<body><h1>Hello from Java in Docker!</h1>"
                        + "<p>This is a simple web-based Java app running inside a container.</p>"
                        + "</body></html>";
            }
            
            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
            exchange.sendResponseHeaders(200, response.getBytes("UTF-8").length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes("UTF-8"));
            }
        }
    }
}
