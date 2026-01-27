package org.manzanita.architecture.structurizr;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.structurizr.Workspace;
import com.structurizr.model.Model;
import com.structurizr.view.ViewSet;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ScraperTest {

    private HttpServer server;
    private String baseUrl;

    private static void respondJson(HttpExchange exchange, String json) throws IOException {
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(200, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private static String workspaceJson(long id, String name) {
        Workspace ws = new Workspace(name, "Test");
        ws.setId(id);
        // Touch model/viewset to ensure minimal structure (some serializers rely on this existing)
        Model model = ws.getModel();
        ViewSet views = ws.getViews();
        // Serialize to JSON using Structurizr util
        try {
            return com.structurizr.util.WorkspaceUtils.toJson(ws, true);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @BeforeEach
    void setUp() throws Exception {
        server = HttpServer.create(new InetSocketAddress(0), 0);
        int port = server.getAddress().getPort();
        baseUrl = "http://localhost:" + port;

        // Root handler - returns an HTML page listing 2 workspaces
        server.createContext("/", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                String html = "<html><body>" +
                        "<div class='workspaceThumbnail'><a href='/workspace/1'>WS1</a></div>" +
                        "<div class='workspaceThumbnail'><a href='/workspace/2'>WS2</a></div>" +
                        "</body></html>";
                byte[] bytes = html.getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().add("Content-Type", "text/html; charset=UTF-8");
                exchange.sendResponseHeaders(200, bytes.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(bytes);
                }
            }
        });

        // JSON endpoints for each workspace
        server.createContext("/workspace/1/json",
                exchange -> respondJson(exchange, workspaceJson(1, "WS1")));
        server.createContext("/workspace/2/json",
                exchange -> respondJson(exchange, workspaceJson(2, "WS2")));

        server.start();
    }

    @AfterEach
    void tearDown() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    void workspacePages_parses_thumbnail_links_and_builds_json_urls() {
        Scraper scraper = new Scraper(baseUrl);
        List<Scraper.WorkspacePageObject> pages = scraper.workspacePages();
        assertEquals(2, pages.size());

        // Ensure URLs are resolved to the /json endpoints
        List<String> urls = pages.stream().map(Scraper.WorkspacePageObject::url).toList();
        assertTrue(urls.contains(URI.create(baseUrl).resolve("/workspace/1/json").toString()));
        assertTrue(urls.contains(URI.create(baseUrl).resolve("/workspace/2/json").toString()));
    }

    @Test
    void leesWorkspace_downloads_and_parses_workspace_json() {
        Scraper scraper = new Scraper(baseUrl);
        List<Scraper.WorkspacePageObject> pages = scraper.workspacePages();
        Workspace ws1 = pages.get(0).readWorkspace();
        assertNotNull(ws1);
        assertEquals("WS1", ws1.getName());
        assertEquals(1L, ws1.getId());

        Workspace ws2 = pages.get(1).readWorkspace();
        assertEquals("WS2", ws2.getName());
        assertEquals(2L, ws2.getId());
    }
}
