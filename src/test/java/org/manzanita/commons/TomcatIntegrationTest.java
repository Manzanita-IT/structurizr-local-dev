package org.manzanita.commons;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.ServerSocket;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class TomcatIntegrationTest {

    private static int findFreePort() throws IOException {
        try (ServerSocket socket = new ServerSocket(0)) {
            socket.setReuseAddress(true);
            return socket.getLocalPort();
        }
    }

    @Test
    void startsAndServesRootContext() throws Exception {
        // Prepare temporary base and webapp directories
        Path baseDir = Files.createTempDirectory("tomcat-base-");
        Path webappDir = Files.createTempDirectory("webapp-");

        // Create minimal WEB-INF/web.xml (Servlet 3.1)
        Path webInf = webappDir.resolve("WEB-INF");
        Files.createDirectories(webInf);
        String webXml = """
                <web-app xmlns="http://xmlns.jcp.org/xml/ns/javaee"
                         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                         xsi:schemaLocation="http://xmlns.jcp.org/xml/ns/javaee http://xmlns.jcp.org/xml/ns/javaee/web-app_3_1.xsd"
                         version="3.1">
                </web-app>
                """;
        Files.writeString(webInf.resolve("web.xml"), webXml, StandardCharsets.UTF_8);

        // Add a static index.html at root
        String indexHtml = "<html><body>It works</body></html>";
        Files.writeString(webappDir.resolve("index.html"), indexHtml, StandardCharsets.UTF_8);

        int port = findFreePort();

        Tomcat tomcat = Tomcat.tomcat(baseDir.toString(), webappDir.toFile());
        tomcat.start();

        // Validate that the server responds on /
        URL url = new URL(tomcat.url() + "/");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(5000);
        int code = conn.getResponseCode();
        assertEquals(200, code);

        String body = new String(conn.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        assertTrue(body.contains("It works"));
    }
}
