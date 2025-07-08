package org.manzanita.commons;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;

@Slf4j
public class Download {

    @SneakyThrows
    public static void download(String url, File destination) {
        log.info("Downloading {} to {}", url, destination.getAbsolutePath());
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet request = new HttpGet(url);
            httpClient.execute(request,
                    (HttpClientResponseHandler<Void>) response -> {
                        InputStream inputStream = response.getEntity().getContent();
                        try (FileOutputStream outputStream = new FileOutputStream(
                                destination)) {

                            byte[] buffer = new byte[1024];
                            int bytesRead;
                            while ((bytesRead = inputStream.read(buffer)) != -1) {
                                outputStream.write(buffer, 0, bytesRead);
                            }
                            log.info("Download finished");
                            return null;
                        }
                    });
        }
    }
}
