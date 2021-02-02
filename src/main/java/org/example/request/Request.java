package org.example.request;

import java.util.List;
import java.util.Map;

public interface Request {

    String getMethod();

    String getPath();

    String getVersion();

    String getHeader(String name);

    Map<String, String> getHeaders();

    String getMessageBody();

    List<String> getQueryParam(String name);

    Map<String, List<String>> getQueryParams();

    List<String> getPostParam(String name);

    Map<String, List<String>> getPostParams();

    List<String> getPart(String name);

    Map<String, List<String>> getParts();

    void loadContent();

}
