package com.fmi.mpr.hw.http;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;

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

            String requestString = read(bis);

            String response = HttpServer.Responses.Welcome();

            HttpRequest request = HttpRequest.fromString(requestString);

            switch(request.type) {
                case GET:
                    response = handleGetRequest(request);
                    break;
                case POST:
                    response = handlePostRequest(request);
                    break;
            }

            System.out.println(request);

            write(ps, response);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String handlePostRequest(HttpRequest request) {
        return HttpServer.Responses.Welcome();
    }

    private String handleGetRequest(HttpRequest request) {
        return HttpServer.Responses.Welcome();
    }

    private String read(BufferedInputStream bis) throws IOException {
            StringBuilder request = new StringBuilder();

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

    private void write(PrintStream ps, String response) {
        ps.println("HTTP/1.0 200 OK");
        ps.println();
        ps.println(Responses.Welcome());
    }


    public static void main(String[] args) throws IOException {
        HttpServer server = new HttpServer();

        server.start();

    }
}
