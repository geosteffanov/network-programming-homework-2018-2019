package com.fmi.mpr.hw.http;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Base64;

public class HttpServer {
    private ServerSocket socket;

    private static class Responses {
        private static String Welcome() {
            return "<!DOCTYPE html>\n" +
                    "<html>\n" +
                    "<head>\n" +
                    "	<title></title>\n" +
                    "</head>\n" +
                    "<body>\n" +
                    "<h2>Welcome</h2>" +
                    "</body>\n" +
                    "</html>";
        }

        private static String BadRequest() {
            return "<!DOCTYPE html>\n" +
                    "<html>\n" +
                    "<head>\n" +
                    "	<title></title>\n" +
                    "</head>\n" +
                    "<body>\n" +
                    "<h2>Bad Request</h2>" +
                    "</body>\n" +
                    "</html>";
        }

        private static String FileSaved() {
            return "<!DOCTYPE html>\n" +
                    "<html>\n" +
                    "<head>\n" +
                    "	<title></title>\n" +
                    "</head>\n" +
                    "<body>\n" +
                    "<h2>File Saved</h2>" +
                    "</body>\n" +
                    "</html>";
        }
    }


    public HttpServer() throws IOException {
        this.socket = new ServerSocket(8080);
    }

    public void start() {
        while(true) {
            listen();
        }
    }

    // listens for connections and handles requests
    private void listen() {
        try {
            Socket client = this.socket.accept();
            handle(client);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handle(Socket client) {
        try (BufferedInputStream bis = new BufferedInputStream(client.getInputStream());
             PrintStream ps = new PrintStream(client.getOutputStream(), true)) {


            byte[] requestData = readData(bis);
            String requestString = read(requestData);




            HttpRequest request = HttpRequest.fromString(requestString);
            request.data = requestData;

            switch(request.type) {
                case GET:
                    handleGetRequest(request, ps, bis);
                    break;
                case POST:
                    handlePostRequest(request, ps, bis);
                    break;
                case FAVICON:
                    handleFaviconRequest(request, ps, bis);
                    break;
                case INVALID:
                    handleInvalidRequest(request, ps, bis);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private byte[] readData(BufferedInputStream bis) throws IOException {
        ByteArrayOutputStream byteOutput = new ByteArrayOutputStream();

        byte[] buffer = new byte[1024];
        int bytesRead = 0;

        while ((bytesRead = bis.read(buffer, 0, 1024)) > 0) {

            byteOutput.write(buffer, 0, bytesRead);

            if (bytesRead < 1024) {
                break;
            }
        }

        return byteOutput.toByteArray();
    }

    private void handleInvalidRequest(HttpRequest request, PrintStream ps, BufferedInputStream bis) {
        ps.println("HTTP/1.1 404 Not Found");
        ps.println();
        ps.println(HttpServer.Responses.BadRequest());
    }

    private void handleFaviconRequest(HttpRequest request, PrintStream ps, BufferedInputStream bis) {
        ps.println("HTTP/1.0 200 OK");
        ps.println();
        ps.println(HttpServer.Responses.Welcome());
    }

    private void handlePostRequest(HttpRequest request, PrintStream ps, BufferedInputStream bis) {
        if (!request.resourceType.equals("videos") && !request.resourceType.equals("images")) {
            ps.println("HTTP/1.0 404 Not Found");
            ps.println(HttpServer.Responses.BadRequest());
        }



        String pathPrefix = "com/fmi/mpr/hw/http/";

        String resourceLocation = pathPrefix + "resources/" + request.resourceType  + "/" + request.resourceName;



        try (FileOutputStream fis = new FileOutputStream(new File(resourceLocation))) {

            InputStream is = new ByteArrayInputStream(request.data);

            byte[] buffer = new byte[1];
            int totalBytes = 0;
            int bytesRead = 0;
            boolean noBody = true;

            totalBytes = readUntilBody(is);

            if (totalBytes ==  -1) {
                ps.println("HTTP/1.1 404 Not Found");
                ps.println();
                ps.println(HttpServer.Responses.BadRequest());
                return;
            }

            buffer = new byte[1024];
            while ((bytesRead = is.read(buffer, 0, 1024)) > 0) {
                fis.write(buffer, 0, bytesRead);

                if (bytesRead < 1024) {
                    break;
                }
            }


        } catch (FileNotFoundException e) {
            e.printStackTrace();
            ps.println("HTTP/1.1 404 Not Found");
            ps.println();
            ps.println("No such resource: " + request.resourceType + "/" + request.resourceName);
        } catch (IOException e) {
            e.printStackTrace();
            ps.println("HTTP/1.1 500 Internal Server Error");
        }

        ps.println("HTTP/1.0 200 OK");
        ps.println();
        ps.println(HttpServer.Responses.FileSaved());
    }

    private String handleGetRequest(HttpRequest request, PrintStream ps, BufferedInputStream bis) {
        String pathPrefix = "com/fmi/mpr/hw/http/";

        String resourceLocation = pathPrefix + "resources/" + request.resourceType  + "/" + request.resourceName;

        try (FileInputStream fis = new FileInputStream(new File(resourceLocation))) {
            ps.println("HTTP/1.0 200 OK");
            ps.println();

            int bytesRead = 0;
            byte[] buffer = new byte[8192];

            while ((bytesRead = fis.read(buffer, 0, 8192)) > 0) {
                ps.write(buffer, 0, bytesRead);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            ps.println("HTTP/1.1 404 Not Found");
            ps.println();
            ps.println("No such resource: " + request.resourceType + "/" + request.resourceName);
        } catch (IOException e) {
            e.printStackTrace();
            ps.println("HTTP/1.1 500 Internal Server Error");
        }

        return HttpServer.Responses.Welcome();
    }

    private String read(byte[] byteData) throws IOException {
            StringBuilder request = new StringBuilder();
            BufferedInputStream bis = new BufferedInputStream(new ByteArrayInputStream(byteData));

            byte[] buffer = new byte[1024];
            int bytesRead = 0;

            while ((bytesRead = bis.read(buffer, 0, 1024)) > 0) {
                request.append(new String(buffer, 0, bytesRead));

                if (bytesRead < 1024) {
                    break;
                }
            }

           return request.toString();
    }


    private static int readUntilCRLF(InputStream is) throws IOException {
        int totalBytesRead = 0;
        boolean carriageReturn = false;

        byte[] buffer = new byte[1];
        int bytesRead = 0;

        while (true) {
            bytesRead = is.read(buffer, 0, 1);
            totalBytesRead += 1;

            if (bytesRead <= 0) {
                return -1;
            }

            // reads \r
            if (buffer[0] == 13) {
                carriageReturn = true;
                continue;
            }

            // reads \n
            if (carriageReturn && (buffer[0] == 10)) {
                break;
            } else {
                carriageReturn = false;
            }

        }
        return totalBytesRead;
    }

    private static int readUntilBody(InputStream is) throws IOException {
        byte[] buffer = new byte[1];
        int totalBytes = 0;
        int bytesRead = 0;

        while(true) {
            totalBytes = readUntilCRLF(is);

            if (totalBytes < 2) {
                return - 1;
            }

            if (totalBytes == 2) {
                return 0;
            }
        }
    }



    public static void main(String[] args) throws IOException {
        HttpServer server = new HttpServer();

        server.start();

    }
}
