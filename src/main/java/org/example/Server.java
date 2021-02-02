package org.example;

import org.example.handlers.Handler;
import org.example.reader.MessageReader;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

public class Server {

    private final Map<String, Map<String, Handler>> handlers = new ConcurrentHashMap<>();
    private ExecutorService executorService;
    private MessageReader messageReader;

    public void listen(int port) {
        if (executorService == null || messageReader == null || handlers.isEmpty()) {
            throw new IllegalStateException("Illegal servers state!");
        }

        System.out.println("Server started");

        try (ServerSocket serverSocket = createServerSocket(port)) {

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Connection accepted");
                executorService.execute(new Connection(clientSocket, messageReader, handlers));
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    public ServerSocket createServerSocket(int port) throws IOException {
        return new ServerSocket(port);
    }

    public void setExecutorService(ExecutorService executorService) {
        this.executorService = executorService;
    }

    public void setMessageReader(MessageReader messageReader) {
        this.messageReader = messageReader;
    }

    public void addHandler(String method, String path, Handler handler) {
        Map<String, Handler> valueMap = handlers.getOrDefault(method, new ConcurrentHashMap<>());
        valueMap.put(path, handler);
        handlers.put(method, valueMap);
    }

    public void addHandler(String method, List<String> paths, Handler handler) {
        for (String path : paths) {
            addHandler(method, path, handler);
        }
    }
}
