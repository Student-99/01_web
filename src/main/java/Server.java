import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {

    final static private int countPool = 64;

    public static void start() {
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

}
