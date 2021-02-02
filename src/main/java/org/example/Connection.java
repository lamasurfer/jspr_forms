package org.example;

import org.example.handlers.Handler;
import org.example.reader.MessageReader;
import org.example.request.Request;
import org.example.request.RequestBuilder;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.List;
import java.util.Map;

public class Connection implements Runnable {

    private final Socket clientSocket;
    private final MessageReader messageReader;

    private final Map<String, Map<String, Handler>> handlers;

    public Connection(Socket clientSocket, MessageReader messageReader, Map<String, Map<String, Handler>> handlers) {
        this.clientSocket = clientSocket;
        this.messageReader = messageReader;
        this.handlers = handlers;
    }

    @Override
    public void run() {
        try (clientSocket;
             final var in = new BufferedInputStream(clientSocket.getInputStream());
             final var out = new BufferedOutputStream(clientSocket.getOutputStream())) {

            final List<String> fullMessage = messageReader.readMessage(in);

            if (!checkRequestLine(fullMessage)) {
                System.out.println("Wrong request line size!");
                return;
            }

            final Request request = new RequestBuilder(fullMessage)
                    .setRequestLine()
                    .setHeaders()
                    .setMessageBody()
                    .setQueryParams()
                    .setPostParams()
                    .setParts()
                    .build();

            System.out.println(request);

            request.loadContent();

            if (!checkHandlers(request)) {
                badRequest(out);
                System.out.println("No suitable handler!");
                return;
            }

            final String method = request.getMethod();
            final String path = request.getPath();

            final Handler handler = handlers.get(method).get(path);
            handler.handle(request, out);

            System.out.println("Request processed!");


        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    public boolean checkRequestLine(List<String> requestAsList) {
        if (requestAsList == null || requestAsList.isEmpty()) {
            return false;
        }
        final String requestLine = requestAsList.get(0);
        final String[] parts = requestLine.split("\\s");

        return parts.length == 3;
    }

    public boolean checkHandlers(Request request) {
        final String method = request.getMethod();
        final String path = request.getPath();

        if (!handlers.containsKey(method)) {
            return false;
        } else {
            var value = handlers.get(method);
            return value.containsKey(path);
        }
    }

    private void badRequest(BufferedOutputStream out) throws IOException {
        out.write((
                "HTTP/1.1 400 Bad Request\r\n" +
                        "Content-Length: 0\r\n" +
                        "Connection: close\r\n" +
                        "\r\n"
        ).getBytes());
        out.flush();
    }
}