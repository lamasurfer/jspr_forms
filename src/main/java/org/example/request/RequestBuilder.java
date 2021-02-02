package org.example.request;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class RequestBuilder {

    private static final String NO_METHOD = "no_method";
    private static final String NO_PATH = "no_path";
    private static final String NO_URI = "no_uri";
    private static final String NO_VERSION = "no_version";
    private static final String NO_MESSAGE_BODY = "no_message";

    private static final String CONTENT_TYPE_HEADER = "Content-Type";
    private static final String CONTENT_TYPE_URLENCODED = "application/x-www-form-urlencoded";
    private static final String CONTENT_TYPE_MULTIPART = "multipart/form-data";
    private final List<String> fullMessage;
    private String method = NO_METHOD;
    private String path = NO_PATH;
    private String uriString = NO_URI;
    private String version = NO_VERSION;
    private Map<String, String> headers = new HashMap<>();
    private String messageBody = NO_MESSAGE_BODY;
    private Map<String, List<String>> queryParams = new HashMap<>();
    private Map<String, List<String>> postParams = new HashMap<>();
    private Map<String, List<String>> parts = new HashMap<>();

    public RequestBuilder(List<String> fullMessage) {
        this.fullMessage = fullMessage;
    }

    public RequestBuilder setRequestLine() {
        if (fullMessage == null || fullMessage.isEmpty()) {
            return this;
        }
        String requestLine = fullMessage.get(0);
        fullMessage.remove(0);
        String[] parts = requestLine.split("\\s");
        this.method = parts[0];
        if (parts[1].contains("?")) {
            String[] temp = parts[1].split("\\?");
            path = temp[0];
        } else {
            path = parts[1];
        }
        this.uriString = parts[1];
        this.version = parts[2];
        return this;
    }

    public RequestBuilder setHeaders() {
        if (fullMessage == null || fullMessage.isEmpty()) {
            return this;
        }
        Iterator<String> it = fullMessage.listIterator();
        while (it.hasNext()) {
            String line = it.next();
            if (line.isBlank()) {
                it.remove();
                break;
            }
            String[] header = line.split(":\\s");
            headers.put(header[0], header[1]);
            it.remove();
        }
        return this;
    }

    public RequestBuilder setMessageBody() {
        if (fullMessage == null || fullMessage.isEmpty()) {
            return this;
        }
        Iterator<String> it = fullMessage.listIterator();
        StringBuilder message = new StringBuilder();
        while (it.hasNext()) {
            String line = it.next();
            if (line.isBlank()) {
                it.remove();
                break;
            }
            message.append(line).append("\n");
            it.remove();
        }
        this.messageBody = message.toString();
        return this;
    }

    public RequestBuilder setQueryParams() {
        if (uriString == null || uriString.isBlank() || uriString.equals(NO_URI)) {
            return this;
        }
        URI uri = null;
        try {
            uri = new URI(uriString);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        if (uri == null) {
            return this;
        }
        List<NameValuePair> list = URLEncodedUtils.parse(uri, StandardCharsets.UTF_8);
        for (NameValuePair pair : list) {
            var values = queryParams.getOrDefault(pair.getName(), new ArrayList<>());
            values.add(pair.getValue());
            queryParams.put(pair.getName(), values);
        }
        return this;
    }

    public RequestBuilder setPostParams() {
        if (headers == null || headers.isEmpty() || !headers.containsKey(CONTENT_TYPE_HEADER) || messageBody.isBlank()) {
            return this;
        }
        String contentType = headers.get(CONTENT_TYPE_HEADER);
        if (contentType == null || !contentType.equals(CONTENT_TYPE_URLENCODED)) {
            return this;
        }
        String[] params = messageBody.trim().split("&");
        for (String param : params) {
            String[] pair = param.split("=");
            if (pair.length != 2) {
                continue;
            }
            var values = postParams.getOrDefault(pair[0], new ArrayList<>());
            values.add(pair[1]);
            postParams.put(pair[0], values);
        }
        return this;
    }

    public RequestBuilder setParts() {
        if (headers == null || headers.isEmpty() || !headers.containsKey(CONTENT_TYPE_HEADER) || messageBody.isBlank()) {
            return this;
        }
        String contentType = headers.get(CONTENT_TYPE_HEADER);
        if (contentType == null || !contentType.startsWith(CONTENT_TYPE_MULTIPART)) {
            return this;
        }
        int boundaryStart = contentType.indexOf('=');
        String delimiter = "--" + contentType.substring(boundaryStart + 1);
        String boundary = delimiter + "\r\n";
        String[] contents = messageBody.split(boundary);
        for (String content : contents) {
            String[] temp = content.split("\r\n\r\n");
            if (temp.length != 2) {
                continue;
            }
            String key = temp[0].trim();
            var values = parts.getOrDefault(key, new ArrayList<>());
            values.add(temp[1]);
            parts.put(key, values);
        }
        return this;
    }

    public Request build() {
        return new RequestImpl(this);
    }

    String getMethod() {
        return method;
    }

    String getPath() {
        return path;
    }

    String getUriString() {
        return uriString;
    }

    String getVersion() {
        return version;
    }

    Map<String, String> getHeaders() {
        return headers;
    }

    RequestBuilder setHeaders(Map<String, String> headers) {
        if (headers == null || headers.isEmpty()) {
            return this;
        }
        this.headers = headers;
        return this;
    }

    String getMessageBody() {
        return messageBody;
    }

    RequestBuilder setMessageBody(String messageBody) {
        if (messageBody == null || messageBody.isBlank()) {
            return this;
        }
        this.messageBody = messageBody;
        return this;
    }

    Map<String, List<String>> getQueryParams() {
        return queryParams;
    }

    RequestBuilder setQueryParams(Map<String, List<String>> queryParams) {
        if (queryParams == null || queryParams.isEmpty()) {
            return this;
        }
        this.queryParams = queryParams;

        return this;
    }

    Map<String, List<String>> getPostParams() {
        return postParams;
    }

    RequestBuilder setPostParams(Map<String, List<String>> postParams) {
        if (postParams == null || postParams.isEmpty()) {
            return this;
        }
        this.postParams = postParams;
        return this;
    }

    Map<String, List<String>> getParts() {
        return parts;
    }

    RequestBuilder setParts(Map<String, List<String>> parts) {
        if (parts == null || parts.isEmpty()) {
            return this;
        }
        this.parts = parts;
        return this;
    }
}