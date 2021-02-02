package org.example.request;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

public class RequestImpl implements Request {

    private static final int MAX_SYMBOLS = 1000;

    private final String method;
    private final String path;
    private final String uri;
    private final String version;
    private final Map<String, String> headers;
    private final String messageBody;
    private final Map<String, List<String>> queryParams;
    private final Map<String, List<String>> postParams;
    private final Map<String, List<String>> parts;


    RequestImpl(String method,
                String path,
                String uri,
                String version,
                Map<String, String> headers,
                String messageBody,
                Map<String, List<String>> queryParams,
                Map<String, List<String>> postParams,
                Map<String, List<String>> parts) {
        this.method = method;
        this.path = path;
        this.uri = uri;
        this.version = version;
        this.headers = headers;
        this.messageBody = messageBody;
        this.queryParams = queryParams;
        this.postParams = postParams;
        this.parts = parts;
    }

    public RequestImpl(RequestBuilder requestBuilder) {
        this.method = requestBuilder.getMethod();
        this.path = requestBuilder.getPath();
        this.uri = requestBuilder.getUriString();
        this.version = requestBuilder.getVersion();
        this.headers = requestBuilder.getHeaders();
        this.messageBody = requestBuilder.getMessageBody();
        this.queryParams = requestBuilder.getQueryParams();
        this.postParams = requestBuilder.getPostParams();
        this.parts = requestBuilder.getParts();
    }

    @Override
    public String getMethod() {
        return method;
    }

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public String getVersion() {
        return version;
    }

    @Override
    public String getHeader(String name) {
        return headers.get(name);
    }

    @Override
    public Map<String, String> getHeaders() {
        return headers;
    }

    @Override
    public String getMessageBody() {
        return messageBody;
    }

    @Override
    public List<String> getQueryParam(String name) {
        return queryParams.get(name);
    }

    @Override
    public Map<String, List<String>> getQueryParams() {
        return queryParams;
    }

    @Override
    public List<String> getPostParam(String name) {
        return postParams.get(name);
    }

    @Override
    public Map<String, List<String>> getPostParams() {
        return postParams;
    }

    @Override
    public List<String> getPart(String name) {
        return parts.get(name);
    }

    @Override
    public Map<String, List<String>> getParts() {
        return parts;
    }

    @Override
    public void loadContent() {
        if (parts == null || parts.isEmpty()) {
            return;
        }
        System.out.println("Processed content: ");
        for (String part : parts.keySet()) {
            List<String> contents = parts.get(part);
            if (part.contains("filename")) {
                File file = new File("downloads");
                if (!file.exists()) {
                    file.mkdir();
                }
                for (String content : contents) {
                    String fileName = part.replaceAll("(.+)(filename=\"(.+)\")(\r\n.+)", "$3");
                    try {
                        FileUtils.writeStringToFile(
                                new File(file.getName(), fileName),
                                content,
                                StandardCharsets.ISO_8859_1);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    System.out.println("Received and downloaded file: " + fileName + "\n");
                }
            } else {
                for (String content : contents) {
                    System.out.println("Received content: " + content);
                }
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("\n>>Request:\n");
        sb.append("method: ").append(method).append("\n");
        sb.append("path: ").append(path).append("\n");
        sb.append("uri: ").append(uri).append("\n");
        sb.append("version: ").append(version).append("\n");
        sb.append(">Headers:\n");
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            sb.append(entry).append("\n");
        }
        sb.append(">Query parameters:\n");
        for (Map.Entry<String, List<String>> entry : queryParams.entrySet()) {
            sb.append(entry).append("\n");
        }
        sb.append(">POST parameters:\n");
        for (Map.Entry<String, List<String>> entry : postParams.entrySet()) {
            sb.append(entry).append("\n");
        }
        sb.append(">Parts:\n");
        for (String key : parts.keySet()) {
            sb.append(key).append(" parts: ").append(parts.get(key).size()).append("\n");
        }
        int messageBodyLength = messageBody.length();
        if (messageBodyLength > MAX_SYMBOLS) {
            String shortMessage = messageBody.substring(0, MAX_SYMBOLS + 1);
            sb.append(">Message:\n").append(shortMessage)
                    .append("\n...and ")
                    .append(messageBodyLength - MAX_SYMBOLS)
                    .append(" more\n");
        } else {
            sb.append(">Message:\n").append(messageBody).append("\n");
        }
        return sb.toString();
    }
}

