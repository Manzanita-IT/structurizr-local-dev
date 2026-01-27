package org.manzanita.commons;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.NginxContainer;
import org.testcontainers.utility.MountableFile;

class DownloadIntegrationTest {

    @Test
    void downloadsFileFromHttpServer() throws Exception {
        // Prepare a file to be served by nginx
        Path tempDir = Files.createTempDirectory("nginx-content");
        Path file = tempDir.resolve("test.txt");
        Files.writeString(file, "hello");

        try (var nginx = new NginxContainer<>("nginx:1.27-alpine")
                .withCopyFileToContainer(MountableFile.forHostPath(file),
                        "/usr/share/nginx/html/test.txt")) {
            nginx.start();

            String url = "http://" + nginx.getHost() + ":" + nginx.getMappedPort(80) + "/test.txt";
            File dest = File.createTempFile("download", ".txt");
            dest.deleteOnExit();

            Download.download(url, dest);

            String content = Files.readString(dest.toPath());
            assertEquals("hello", content);
        }
    }
}
