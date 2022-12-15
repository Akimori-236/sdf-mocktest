// TODO: Change to your package 
package mywebserver;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class HttpWriter {

    private final OutputStream out;

    public HttpWriter(OutputStream out) {
        this.out = out;
    }

    public void flush() throws Exception {
        this.out.flush();
    }

    public void close() throws Exception {
        out.flush();
        out.close();
    }

    public void writeString() throws Exception {
        writeString("");
    }

    public void writeString(String line) throws Exception {
        writeBytes("%s\r\n".formatted(line).getBytes("utf-8"));
    }

    public void writeBytes(byte[] buffer) throws Exception {
        writeBytes(buffer, 0, buffer.length);
    }

    public void writeBytes(byte[] buffer, int start, int offset) throws Exception {
        out.write(buffer, start, offset);
    }

    public void sendHTML(String resource, List<String> docRoot) {
        for (String dir : docRoot) {
            Path path = Paths.get(dir, resource);
            File f = path.toFile();
            if (f.exists()) {
                // SENDING
                System.out.println("File exists, sending file...");
                File htmlfile = new File(dir + resource);
                try {
                    BufferedReader reader = new BufferedReader(new FileReader(htmlfile));
                    // SEND HTTP HEADER
                    this.writeString("HTTP/1.1 200 OK");
                    this.writeString("Content-Type: text/html");
                    this.writeString("Content-Length: " + htmlfile.length());
                    this.writeString("\r\n");

                    // SEND FILE ITSELF
                    String line = reader.readLine();
                    while (line != null) {
                        this.writeString(line);
                        line = reader.readLine();
                    }
                    this.flush();
                    System.out.println("Finished sending file of " + htmlfile.length() + " bytes");
                    reader.close();
                } catch (Exception e) {
                }
            }
            break;
        }
    }

    public void sendPNG(String resource, List<String> docRoot) {
        for (String dir : docRoot) {
            Path path = Paths.get(dir, resource);
            File f = path.toFile();
            if (f.exists()) {
                // SENDING
                System.out.println("File exists, sending file...");
                File htmlfile = new File(dir + resource);
                try {
                    // TODO:need image reader
                    BufferedReader reader = new BufferedReader(new FileReader(htmlfile));
                    // SEND HTTP HEADER
                    this.writeString("HTTP/1.1 200 OK");
                    this.writeString("Content-Type: image/png");
                    this.writeString("Content-Length: " + htmlfile.length());
                    this.writeString("\r\n");

                    // SEND FILE ITSELF
                    String line = reader.readLine(); // TODO:need to read bytes
                    while (line != null) {
                        this.writeString(line); // TODO:write bytes
                        line = reader.readLine(); // TODO:need to read bytes
                    }
                    this.flush();
                    System.out.println("Finished sending file of " + htmlfile.length() + " bytes");
                    reader.close();
                } catch (Exception e) {
                }
            }
            break;
        }
    }
}
