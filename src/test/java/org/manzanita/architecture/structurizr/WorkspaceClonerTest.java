package org.manzanita.architecture.structurizr;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.manzanita.architecture.structurizr.instance.RemoteStructurizrInstance;
import org.manzanita.architecture.structurizr.instance.StructurizrInstance;
import com.structurizr.Workspace;
import com.structurizr.api.WorkspaceMetadata;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class WorkspaceClonerTest {

    private HttpServer server;
    private String bronUrl;

    private static void respond(HttpExchange exchange, int status, String contentType, String body)
            throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", contentType);
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    @BeforeEach
    void setUp() throws Exception {
        server = HttpServer.create(new InetSocketAddress(0), 0);
        int port = server.getAddress().getPort();
        bronUrl = "http://localhost:" + port;

        server.createContext("/", exchange -> {
            String html = "<div class='workspaceThumbnail'><a href='/workspace/42'>WS</a></div>";
            respond(exchange, 200, "text/html; charset=UTF-8", html);
        });
        server.createContext("/workspace/42/json", exchange -> {
            Workspace ws = new Workspace("TeKlonen", "");
            ws.setId(42);
            String json;
            try {
                json = com.structurizr.util.WorkspaceUtils.toJson(ws, true);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            respond(exchange, 200, "application/json; charset=UTF-8", json);
        });
        server.start();
    }

    @AfterEach
    void tearDown() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    void cloneTo_creates_and_puts_when_workspace_not_present() {
        // Mocks for target instance
        StructurizrInstance doel = mock(RemoteStructurizrInstance.class);
        when(doel.url()).thenReturn("http://target");

        AdminApiClient admin = mock(AdminApiClient.class);
        when(doel.createAdminApiClient()).thenReturn(admin);

        // Simulate that the workspace does not yet exist on target
        when(admin.getWorkspace(any(Workspace.class))).thenReturn(Optional.empty());

        WorkspaceMetadata created = mock(WorkspaceMetadata.class);
        when(created.getId()).thenReturn(100);
        when(created.getApiKey()).thenReturn("apiKey");
        when(created.getApiSecret()).thenReturn("apiSecret");
        when(admin.createWorkspace()).thenReturn(created);

        WorkspaceApiClient workspaceApiClient = mock(WorkspaceApiClient.class);
        when(doel.createWorkspaceApiClient(created)).thenReturn(workspaceApiClient);

        WorkspaceCloner cloner = new WorkspaceCloner(bronUrl);
        cloner.cloneTo(doel);

        // Verify flow
        verify(admin).getWorkspace(any(Workspace.class));
        verify(admin).createWorkspace();

        ArgumentCaptor<Workspace> workspaceCaptor = ArgumentCaptor.forClass(Workspace.class);
        verify(workspaceApiClient).putWorkspace(eq(100L), workspaceCaptor.capture());
        Workspace cloned = workspaceCaptor.getValue();
        assertEquals(100L, cloned.getId()); // id should be set before putting
        assertEquals("TeKlonen", cloned.getName());
    }

    @Test
    void cloneTo_does_nothing_when_workspace_already_present() {
        StructurizrInstance doel = mock(RemoteStructurizrInstance.class);
        when(doel.url()).thenReturn("http://target");

        AdminApiClient admin = mock(AdminApiClient.class);
        when(doel.createAdminApiClient()).thenReturn(admin);

        // Simulate that the workspace already exists
        WorkspaceMetadata existing = mock(WorkspaceMetadata.class);
        when(admin.getWorkspace(any(Workspace.class))).thenReturn(Optional.of(existing));

        WorkspaceCloner cloner = new WorkspaceCloner(bronUrl);
        cloner.cloneTo(doel);

        verify(admin, never()).createWorkspace();
        verify(doel, never()).createWorkspaceApiClient(any());
    }
}
