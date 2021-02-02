package org.example.handlers;

import org.example.request.Request;

import java.io.BufferedOutputStream;
import java.io.IOException;

public class OkHandler implements Handler {

    @Override
    public void handle(Request request, BufferedOutputStream responseStream) throws IOException {
        responseStream.write((
                "HTTP/1.1 200 OK\r\n" +
                        "Content-Length: 0\r\n" +
                        "Connection: close\r\n" +
                        "\r\n"
        ).getBytes());
        responseStream.flush();
    }
}
