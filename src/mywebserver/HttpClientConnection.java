package mywebserver;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class HttpClientConnection implements Runnable {

    Socket socket;
    List<String> docRoot;
    BufferedReader br;
    BufferedWriter bw;
    OutputStream os;
    String threadname;

    public HttpClientConnection(Socket socket, List<String> docRoot) {
        this.socket = socket;
        this.docRoot = docRoot;
        this.threadname = Thread.currentThread().getName();

        try {
            InputStream is = this.socket.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            this.br = new BufferedReader(isr);
            this.os = this.socket.getOutputStream();
            OutputStreamWriter osw = new OutputStreamWriter(this.os);
            this.bw = new BufferedWriter(osw);

        } catch (IOException e) {
            // e.printStackTrace();
            System.err.println("ERROR> Unable to establish IO streams with client");
        }
    }

    @Override
    public void run() {
        System.out.println(this.threadname + "> Thread running...");

        // READ REQUEST
        try {
            String request = this.br.readLine();
            System.out.println(this.threadname + "> Request header> " + request);
            String[] requestTerms = request.split(" ");
            String method = requestTerms[0];
            String resource;
            if (requestTerms[1].trim().equals("/")) {
                resource = "/index.html";
            } else {
                resource = requestTerms[1];
            }
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
                HttpWriter hw = new HttpWriter(this.os);
                System.out.println(this.threadname + "> Resource found (" + resource + ")");
                if (resource.endsWith(".html")) {
                    System.out.println(this.threadname + "> Sending html file...");
                    this.sendHTML(resource, docRoot);
                } else if (resource.endsWith(".css")) {
                    System.out.println(this.threadname + "> Sending css file...");
                    this.sendCSS(resource, docRoot);
                } else if (resource.endsWith(".png")) {
                    System.out.println(this.threadname + "> Sending png file...");
                    this.sendPNG(resource, docRoot);
                }
                try {
                    hw.flush();
                    hw.close();
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }

        } catch (IOException e) {
            System.err.println(this.threadname + "> ERROR> Unable to read request");
        }
    }

    private Boolean validMethod(String method) {
        Boolean valid = method.equalsIgnoreCase("GET");
        if (!valid) {
            System.err.println("ERROR 405");
            try {
                HttpWriter hw = new HttpWriter(this.os);
                hw.writeString("HTTP/1.1 405 Method Not Allowed\r\n\r\n" + method + " not supported\r\n");
                hw.flush();
                hw.close();
            } catch (Exception e) {
                System.err.println(this.threadname + "> ERROR> Unable to write error 405 to client");
            }
        }
        return valid;
    }

    private Boolean resourceFound(String resource) {
        Boolean found = false;
        for (String dir : docRoot) {
            Path path = Paths.get(dir, resource);
            File file = path.toFile();
            if (file.exists()) {
                found = true;
            }
        }
        if (!found) {
            System.err.println("ERROR 404");
            try {
                HttpWriter hw = new HttpWriter(this.os);
                hw.writeString("HTTP/1.1 404 Not Found\r\n\r\n" + resource + " not found\r\n");
                hw.flush();
                hw.close();
            } catch (Exception e) {
                System.err.println(this.threadname + "> ERROR> Unable to write error 404 to client");
            }
        }
        return found;
    }

    public void sendHTML(String resource, List<String> docRoot) {
        for (String dir : docRoot) {
            Path path = Paths.get(dir, resource);
            File file = path.toFile();
            if (file.exists()) {
                // SENDING
                System.out.println(Thread.currentThread().getName() + "> File exists, sending file...");
                try {
                    FileReader fr = new FileReader(file);
                    BufferedReader reader = new BufferedReader(fr);
                    // SEND HTTP HEADER
                    HttpWriter hw = new HttpWriter(this.os);
                    hw.writeString("HTTP/1.1 200 OK");
                    hw.writeString("Content-Type: text/html");
                    hw.writeString("Content-Length: " + file.length());
                    hw.writeString("\r\n");

                    // SEND FILE ITSELF
                    String line = reader.readLine();
                    while (line != null) {
                        hw.writeString(line);
                        line = reader.readLine();
                    }
                    hw.flush();
                    System.out.println(Thread.currentThread().getName() + "> Finished sending file of " + file.length()
                            + " bytes");
                    fr.close();
                    hw.flush();
                    hw.close();
                } catch (Exception e) {
                }
            } // end if
            break;
        } // end for
    }

    public void sendCSS(String resource, List<String> docRoot) {
        for (String dir : docRoot) {
            Path path = Paths.get(dir, resource);
            File file = path.toFile();
            if (file.exists()) {
                // SENDING
                System.out.println(Thread.currentThread().getName() + "> File exists, sending file...");
                try {
                    FileReader fr = new FileReader(file);
                    BufferedReader reader = new BufferedReader(fr);
                    // SEND HTTP HEADER
                    HttpWriter hw = new HttpWriter(this.os);
                    hw.writeString("HTTP/1.1 200 OK");
                    hw.writeString("Content-Type: text/css");
                    // hw.writeString("Content-Length: " + file.length());
                    hw.writeString("\r\n");

                    // SEND FILE ITSELF
                    String line;
                    while ((line = reader.readLine()) != null) {
                        System.out.println("CSS> " + line);
                        hw.writeString(line);
                    }
                    fr.close();
                    hw.flush();
                    hw.close();
                    System.out.println(Thread.currentThread().getName() + "> Finished sending file of " + file.length()
                            + " bytes");

                } catch (Exception e) {
                    e.printStackTrace();
                }
            } // end if
            break;
        } // end for
    }

    public void sendPNG(String resource, List<String> docRoot) {
        for (String dir : docRoot) {
            Path path = Paths.get(dir, resource);
            File file = path.toFile();
            if (file.exists()) {
                // SENDING
                System.out.println(Thread.currentThread().getName() + "> File exists, sending file...");
                try {
                    // SEND HTTP HEADER
                    FileInputStream fis = new FileInputStream(dir + resource);
                    HttpWriter hw = new HttpWriter(this.os);
                    hw.writeString("HTTP/1.1 200 OK");
                    hw.writeString("Content-Type: image/png");
                    // hw.writeString("Content-Length: " + file.length());
                    hw.writeString("\r\n");

                    // READ IMG FILE
                    // byte[] data = Files.readAllBytes(Paths.get(file.getPath()));
                    // SEND FILE ITSELF
                    byte[] data;
                    while ((data = Files.readAllBytes(path)) != null) {
                        hw.writeBytes(data);// NEED TO SEND IN ONE SHOT
                    }
                    hw.flush();
                    hw.close();
                    System.out.println(Thread.currentThread().getName() + "> Finished sending file of " + file.length()
                            + " bytes");
                } catch (Exception e) {
                }
            } // end if
            break;
        } // end for
    }
}
