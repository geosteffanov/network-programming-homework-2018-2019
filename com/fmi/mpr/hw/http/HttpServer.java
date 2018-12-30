package com.fmi.mpr.hw.http;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;

public class HttpServer {
    private ServerSocket socket;


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

            String request = read(bis);

            System.out.println(request);

        } catch (IOException e) {
            e.printStackTrace();
        }
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



    public static void main(String[] args) throws IOException {
        HttpServer server = new HttpServer();

        server.start();

    }
}
