import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ResponseThread implements Runnable {

    final static List<String> availablePages = List.of("/index.html", "/spring.svg", "/spring.png", "/resources.html",
            "/styles.css",
            "/app.js", "/links.html", "/forms.html", "/classic.html", "/events.html", "/events.js");

    private BufferedReader in;
    private BufferedOutputStream out;

    public ResponseThread(Socket socket) throws IOException {
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new BufferedOutputStream(socket.getOutputStream());
    }

    @Override
    public void run() {
        try {
            // read only request line for simplicity
            // must be in form GET /path HTTP/1.1
            final var requestLine = in.readLine();
            final var parts = requestLine.split(" ");

            if (parts.length != 3) {
                out.write(createResponse("HTTP/1.1 400 Bad Request",
                        Map.of(
                                "Content-Length", "0",
                                "Connection", "close")).getBytes()
                );
                out.flush();
                return;
            }

            final var path = parts[1];
            if (!availablePages.contains(path)) {
                out.write(createResponse("HTTP/1.1 404 Not Found",
                        Map.of(
                                "Content-Length", "0",
                                "Connection", "close")).getBytes()
                );
                out.flush();
                return;
            }
            final var content = createResponseWithAFile(path, filesAndTheirVariablesWithValues());
            out.write(content.getBytes());
            out.flush();
        } catch (IOException exception) {
            exception.printStackTrace();
        }


    }

    private String createResponseWithAFile(String filePath, Map<String, Map<String, String>> fileVariables) throws IOException {
        final var currentFilePath = Path.of(".", "public", filePath);
        final var mimeType = Files.probeContentType(currentFilePath);
        var template = Files.readString(currentFilePath);
        String response;

        var currentFileVariables = fileVariables.get(filePath);
        if (currentFileVariables != null) {
            for (Map.Entry<String, String> entry : currentFileVariables.entrySet()) {
                template = template.replace(entry.getKey(), entry.getValue());
            }
        }

        response = createResponse("HTTP/1.1 200 OK",
                Map.of(
                        "Content-Type", mimeType,
                        "Content-Length", String.valueOf(template.getBytes().length),
                        "Connection", "close"));
        response += "\r\n";
        response += template;
        return response;
    }

    private String createResponse(String startingLine, Map<String, String> headers) {
        StringBuilder responseBuilder = new StringBuilder();
        responseBuilder.append(startingLine).append("\r\n");
        var headersIterator = headers.entrySet().iterator();

        while (headersIterator.hasNext()) {
            var header = headersIterator.next();
            responseBuilder.append(header.getKey()).append(": ").append(header.getValue()).append("\r\n");
        }
        responseBuilder.append("\r\n");
        return responseBuilder.toString();
    }

    private Map<String, Map<String, String>> filesAndTheirVariablesWithValues() {
        Map<String, Map<String, String>> maps = new HashMap<>();
        Map<String, String> classicFileVariables = Map.of("{time}", LocalTime.now().toString());
        maps.put("/classic.html", classicFileVariables);

        return maps;
    }
}
