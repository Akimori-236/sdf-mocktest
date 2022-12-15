package mywebserver;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        int port;
        List<String> docRoot = new ArrayList<>();

        // Handling cmd-line arguments
        List<String> arguments = Arrays.asList(args);
        int index;
        if ((index = arguments.indexOf("--port")) != -1) {
            port = Integer.parseInt(arguments.get(index + 1));
        } else {
            // default
            port = 3000;
        }
        if ((index = arguments.indexOf("--docRoot")) != -1) {
            String[] dR = arguments.get(index + 1).split(":");
            for (String dir : dR) {
                docRoot.add(dir);
            }
            
        } else {
            // default
            docRoot.add("./static");
            // docRoot.add("./target");
        }
        // CHECKS
        System.out.println("PORT> " + Integer.toString(port));
        System.out.println("DOCROOT> " + docRoot);

        // START SERVER
        HttpServer.start(port, docRoot);

    }
}
