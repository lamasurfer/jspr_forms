package org.example.test;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class PostFile {
    public static void main(String[] args) throws IOException {

        CloseableHttpClient httpclient = HttpClients.createDefault();
        try {
            String fileName = "favicon.ico";
            File file = new File(fileName);
            URI uri = new URIBuilder()
                    .setScheme("http")
                    .setHost("localhost")
                    .setPort(9999)
                    .setPath("/messages")
                    .build();

            StringBody stringBody1 = new StringBody("message", ContentType.MULTIPART_FORM_DATA);
            StringBody stringBody2 = new StringBody("message", ContentType.MULTIPART_FORM_DATA);
            StringBody stringBody3 = new StringBody("message1", ContentType.MULTIPART_FORM_DATA);
            FileBody fileBody = new FileBody(file, ContentType.MULTIPART_FORM_DATA);

            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
            builder.addPart("message", stringBody1);
            builder.addPart("message", stringBody2);
            builder.addPart("message1", stringBody3);
            builder.addPart("image", fileBody);
            HttpEntity entity = builder.build();
            HttpPost httpPost = new HttpPost(uri);

            httpPost.setEntity(entity);


            CloseableHttpResponse response = httpclient.execute(httpPost);
            System.out.println(response);

        } catch (URISyntaxException | IOException e) {
            System.out.println(e.getMessage());
            httpclient.close();
        }
    }
}
