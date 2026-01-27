package org.manzanita.architecture.structurizr.instance;

import com.structurizr.api.WorkspaceMetadata;
import java.awt.Desktop;
import java.net.URI;
import java.util.Optional;
import lombok.SneakyThrows;
import org.manzanita.architecture.structurizr.AdminApiClient;
import org.manzanita.architecture.structurizr.WorkspaceApiClient;
import org.manzanita.architecture.structurizr.WorkspaceCloner;

public interface StructurizrInstance {

    static StructurizrInstance resolve() {
        return fromEnvironment().orElseGet(StructurizrInstance::local);
    }

    private static Optional<StructurizrInstance> fromEnvironment() {
        return Optional.ofNullable(System.getenv("STRUCTURIZR_URL"))
                .map(url -> new RemoteStructurizrInstance(
                        url,
                        System.getenv("STRUCTURIZR_API_KEY")));
    }

    private static StructurizrInstance local() {
        var instance = LocalStructurizrInstance.start();

        LocalProperties.reference()
                .ifPresent(reference -> new WorkspaceCloner(reference).cloneTo(instance));
        return instance;
    }

    default AdminApiClient createAdminApiClient() {
        return new AdminApiClient(
                this,
                new com.structurizr.api.AdminApiClient(apiUrl(), null, adminApiKey()));
    }

    default WorkspaceApiClient createWorkspaceApiClient(WorkspaceMetadata workspaceMetadata) {
        com.structurizr.api.WorkspaceApiClient client = new com.structurizr.api.WorkspaceApiClient(
                apiUrl(),
                workspaceMetadata.getApiKey(),
                workspaceMetadata.getApiSecret());
        client.setWorkspaceArchiveLocation(null); // voorkom lokaal uitschrijven van .json
        return new WorkspaceApiClient(client);
    }

    default String apiUrl() {
        return url() + "/api";
    }

    @SneakyThrows
    default void openInBrowser() {
        if (Desktop.isDesktopSupported() && Desktop.getDesktop()
                .isSupported(Desktop.Action.BROWSE)) {
            Desktop.getDesktop().browse(new URI(url()));
        }
    }

    void stop();

    String url();

    String adminApiKey();
}
