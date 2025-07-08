package org.manzanita.architecture.structurizr;

import org.manzanita.architecture.structurizr.local.LocalStructurizr;
import com.structurizr.api.WorkspaceMetadata;
import java.awt.Desktop;
import java.net.URI;
import java.util.Optional;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public record StructurizrInstance(
        String url,
        String adminApiKey) {

    public AdminApiClient createAdminApiClient() {
        return new AdminApiClient(
                this,
                new com.structurizr.api.AdminApiClient(apiUrl(), null, adminApiKey));
    }

    public WorkspaceApiClient createWorkspaceApiClient(WorkspaceMetadata workspaceMetadata) {
        com.structurizr.api.WorkspaceApiClient client = new com.structurizr.api.WorkspaceApiClient(
                apiUrl(),
                workspaceMetadata.getApiKey(),
                workspaceMetadata.getApiSecret());
        client.setWorkspaceArchiveLocation(null); // voorkom local uitschrijven van .json
        return new WorkspaceApiClient(client);
    }

    private String apiUrl() {
        return url + "/api";
    }

    public static StructurizrInstance resolve() {
        return fromEnvironment().orElseGet(StructurizrInstance::local);
    }

    private static Optional<StructurizrInstance> fromEnvironment() {
        return Optional.ofNullable(System.getenv("STRUCTURIZR_URL"))
                .map(url -> new StructurizrInstance(
                        url,
                        System.getenv("STRUCTURIZR_API_KEY")));
    }

    private static StructurizrInstance local() {
        return LocalStructurizr.start();
    }

    @SneakyThrows
    public void openInBrowser() {
        if (Desktop.isDesktopSupported() && Desktop.getDesktop()
                .isSupported(Desktop.Action.BROWSE)) {
            log.info("Structurizr is being opened in the browser...");
            Desktop.getDesktop().browse(new URI(url));
        }
    }
}
