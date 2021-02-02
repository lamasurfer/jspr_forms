package org.example.reader;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.List;

public interface MessageReader {

    List<String> readMessage(BufferedInputStream in) throws IOException;
}
