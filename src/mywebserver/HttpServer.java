package mywebserver;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HttpServer {
    public static void start(int port, List<String> docRoot) {
        // docRoot validity
        if (!docRootValid(docRoot)) {
            System.exit(1);
        }
        ;

        // THREADPOOL
        ExecutorService threadpool = Executors.newFixedThreadPool(3);

        try {
            ServerSocket server = new ServerSocket(port);
            System.out.println("Server listening on port " + port + "...");
            while (true) {
                Socket socket = server.accept();
                System.out.println("~New Connection~");
                HttpClientConnection newClient = new HttpClientConnection(socket, docRoot);
                threadpool.submit(newClient);
            }

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("ERROR> Unable to start server, or accept connections.");
        }
    }

    public static Boolean docRootValid(List<String> docRoot) {
        Boolean valid = true;
        for (String dir : docRoot) {
            Path path = Paths.get(dir);
            if (Files.exists(path) == false) {
                valid = false;
            }
            System.out.println(dir + " validity> " + valid);
        }
        return valid;
    }
}
