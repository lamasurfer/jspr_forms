package org.example;

import org.example.handlers.ClassicHandlerImpl;
import org.example.handlers.FileHandlerImpl;
import org.example.handlers.OkHandler;
import org.example.reader.MessageReader;
import org.example.reader.MessageReaderImpl;

import java.util.List;
import java.util.concurrent.Executors;

public class Main {

    public static void main(String[] args) {

        final List<String> VALID_PATHS = List.of("/index.html", "/spring.svg", "/spring.png", "/resources.html",
                "/styles.css", "/app.js", "/classic.html", "/links.html", "/forms.html", "/events.html", "/events.js",
                "/favicon.ico", "/default-get.html", "/default-post.html", "/multi-post.html");

        final int MAX_CONNECTIONS = 64;
        final int PORT = 9999;
        final int BUFFER_SIZE = 4096;

        final Server server = new Server();
        final MessageReader messageReader = new MessageReaderImpl(BUFFER_SIZE);

        server.setExecutorService(Executors.newFixedThreadPool(MAX_CONNECTIONS));
        server.setMessageReader(messageReader);

        server.addHandler("GET", VALID_PATHS, new FileHandlerImpl());
        server.addHandler("GET", "/classic.html", new ClassicHandlerImpl());
        server.addHandler("GET", "/messages", new OkHandler());
        server.addHandler("POST", "/messages", new OkHandler());

        server.listen(PORT);

    }
}
