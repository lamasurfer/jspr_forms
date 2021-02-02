package org.example.reader;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class MessageReaderImpl implements MessageReader {

    private static final byte[] REQUEST_LINE_DELIMITER = new byte[]{'\r', '\n'};
    private static final byte[] HEADERS_DELIMITER = new byte[]{'\r', '\n', '\r', '\n'};

    private final int bufferSize;

    public MessageReaderImpl(int bufferSize) {
        this.bufferSize = bufferSize;
    }

    private static Optional<String> extractHeader(List<String> headers, String header) {
        return headers.stream()
                .filter(o -> o.startsWith(header))
                .map(o -> o.substring(o.indexOf(" ")))
                .map(String::trim)
                .findFirst();
    }

    // from google guava with modifications
    private static int indexOf(byte[] array, byte[] target, int start, int max) {
        outer:
        for (int i = start; i < max - target.length + 1; i++) {
            for (int j = 0; j < target.length; j++) {
                if (array[i + j] != target[j]) {
                    continue outer;
                }
            }
            return i;
        }
        return -1;
    }

    @Override
    public List<String> readMessage(BufferedInputStream in) throws IOException {

        final byte[] buffer = new byte[bufferSize];

        List<String> fullMessage = new ArrayList<>();

        in.mark(buffer.length);
        final int read = in.read(buffer);

        final var requestLineEnd = indexOf(buffer, REQUEST_LINE_DELIMITER, 0, read);

        final String requestLine = new String(Arrays.copyOf(buffer, requestLineEnd));

        fullMessage.add(requestLine);

        final int headersStart = requestLineEnd + REQUEST_LINE_DELIMITER.length;
        final int headersEnd = indexOf(buffer, HEADERS_DELIMITER, headersStart, read);

        in.reset();
        in.skip(headersStart);

        final byte[] headersBytes = in.readNBytes(headersEnd - headersStart);
        final List<String> headers = Arrays.asList(new String(headersBytes).split("\r\n"));

        fullMessage.addAll(headers);

        in.skip(HEADERS_DELIMITER.length);
        String body = null;
        final var contentLength = extractHeader(headers, "Content-Length");
        if (contentLength.isPresent()) {
            final var length = Integer.parseInt(contentLength.get());
            final var bodyBytes = in.readNBytes(length);

            body = new String(bodyBytes, StandardCharsets.ISO_8859_1);
            fullMessage.add("");
            fullMessage.add(body);
        }
        return fullMessage;
    }
}
