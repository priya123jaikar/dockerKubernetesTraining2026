import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class Main {
    public static void main(String[] args) throws Exception {
        int port = 8060;

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Java HTTP server started on port " + port);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                handleClient(clientSocket);
            }
        }
    }

    private static void handleClient(Socket clientSocket) {
        try (Socket socket = clientSocket;
             BufferedReader reader = new BufferedReader(
                     new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
             BufferedWriter writer = new BufferedWriter(
                     new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8))) {

            String line;
            while ((line = reader.readLine()) != null && !line.isEmpty()) {
                // Consume request headers until the blank line.
            }

            String body = "Hello from Java server running in Docker on port 8060!\n";
            writer.write("HTTP/1.1 200 OK\r\n");
            writer.write("Content-Type: text/plain; charset=UTF-8\r\n");
            writer.write("Content-Length: " + body.getBytes(StandardCharsets.UTF_8).length + "\r\n");
            writer.write("Connection: close\r\n");
            writer.write("\r\n");
            writer.write(body);
            writer.flush();
        } catch (Exception e) {
            System.err.println("Request failed: " + e.getMessage());
        }
    }
}
