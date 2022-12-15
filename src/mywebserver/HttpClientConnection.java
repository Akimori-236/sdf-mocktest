package mywebserver;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class HttpClientConnection implements Runnable {

    Socket socket;
    List<String> docRoot;
    BufferedReader br;
    BufferedWriter bw;
    OutputStream os;
    HttpWriter hw;

    public HttpClientConnection(Socket socket, List<String> docRoot) {
        this.socket = socket;
        this.docRoot = docRoot;

        try {
            InputStream is = this.socket.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            br = new BufferedReader(isr);
            os = this.socket.getOutputStream();
            OutputStreamWriter osw = new OutputStreamWriter(this.os);
            bw = new BufferedWriter(osw);
            hw = new HttpWriter(this.os);

        } catch (IOException e) {
            // e.printStackTrace();
            System.err.println("ERROR> Unable to establish IO streams with client");
        }
    }

    @Override
    public void run() {
        System.out.println("Thread running...");

        String request;
        String method;
        String resource;

        // READ REQUEST
        try {
            request = this.br.readLine();
            System.out.println("Request header> " + request);
            String[] requestTerms = request.split(" ");
            method = requestTerms[0];
            if (requestTerms[1] == "/") {
                resource = "/index.html";
            } else {
                resource = requestTerms[1];
            }
            // System.out.println("Request method> " + method);

            // INVALID METHOD
            if (!validMethod(method)) {
                this.socket.close();
                return;
            }
            // FILE DOESNT EXIST
            if (!resourceFound(resource)) {
                this.socket.close();
                return;
            } else {
                // FILE FOUND
                System.out.println("Resource found");
                sendHTML(resource);
            }

        } catch (IOException e) {
            System.err.println("ERROR> Unable to read request");
        }

    }

    private Boolean validMethod(String method) {
        Boolean valid = method.equalsIgnoreCase("GET");
        if (!valid) {
            System.err.println("ERROR 405");
            try {
                hw.writeString("HTTP/1.1 405 Method Not Allowed\r\n\r\n" + method + " not supported\r\n");
            } catch (Exception e) {
                System.err.println("ERROR> Unable to write error 405 to client");
            }
        }
        return valid;
    }

    private Boolean resourceFound(String resource) {
        Boolean found = false;
        for (String dir : docRoot) {
            Path path = Paths.get(dir, resource);
            File f = path.toFile();
            if (f.exists()) {
                found = true;
            }
        }
        if (!found) {
            System.err.println("ERROR 404");
            try {
                hw.writeString("HTTP/1.1 404 Not Found\r\n\r\n" + resource + " not found\r\n");
            } catch (Exception e) {
                System.err.println("ERROR> Unable to write error 404 to client");
            }
        }
        return found;
    }

    private void sendHTML(String resource) {
        for (String dir : this.docRoot) {
            Path path = Paths.get(dir, resource);
            File f = path.toFile();
            if (f.exists()) {
                // SENDING
                System.out.println("File exists, sending file...");
                File htmlfile = new File(dir + resource);
                try {
                    BufferedReader reader = new BufferedReader(new FileReader(htmlfile));
                    // SEND HTTP HEADER
                    hw.writeString("HTTP/1.1 200 OK");
                    hw.writeString("Content-Type: text/html");
                    hw.writeString("Content-Length: " + htmlfile.length());
                    hw.writeString("\r\n");

                    // SEND FILE ITSELF
                    String line = reader.readLine();
                    while (line != null) {
                        hw.writeString(line);
                        line = reader.readLine();
                    }
                    hw.flush();
                    System.out.println("Finished sending file of " + htmlfile.length() + " bytes");
                    reader.close();
                } catch (Exception e) {
                }
            }
            break;
        }
    }
}
