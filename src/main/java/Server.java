import java.io.IOException;
import java.net.ServerSocket;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    final static List<String> validPaths = List.of("/index.html", "/spring.svg", "/spring.png", "/resources.html",
            "/styles.css",
            "/app.js", "/links.html", "/forms.html", "/classic.html", "/events.html", "/events.js");

    final static int countPool = 64;

    public static void main(String[] args) {
        ExecutorService poolExecutor = Executors.newFixedThreadPool(countPool);

        try (final var serverSocket = new ServerSocket(9999)) {
            while (true) {
                final var socket = serverSocket.accept();
                poolExecutor.execute(new ResponseThread(socket));
            }
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    public static Map<String, Map<String, String>> filesAndTheirVariablesWithValues() {
        Map<String, Map<String, String>> maps = new HashMap<>();
        Map<String,String> classicFileVariables = Map.of("{time}", LocalTime.now().toString());
        maps.put("/classic.html",classicFileVariables);

        return maps;
    }
}
