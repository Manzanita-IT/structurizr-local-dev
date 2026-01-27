package org.manzanita.architecture.structurizr;

import static lombok.AccessLevel.PRIVATE;

import com.structurizr.Workspace;
import com.structurizr.api.WorkspaceMetadata;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.manzanita.architecture.structurizr.instance.StructurizrInstance;

@RequiredArgsConstructor(access = PRIVATE)
public class WorkspaceWriter {

    private final StructurizrInstance instance;
    private final AdminApiClient adminApiClient;

    public static WorkspaceWriter from(StructurizrInstance instance) {
        AdminApiClient adminApiClient = instance.createAdminApiClient();
        return new WorkspaceWriter(instance, adminApiClient);
    }

    public void write(Landscape landscape) {
        write(landscape.getWorkspace());
    }

    @SneakyThrows
    public void write(Workspace workspace) {
        WorkspaceMetadata workspaceMetadata = adminApiClient.getOrCreateWorkspace(workspace);
        WorkspaceApiClient workspaceApiClient = instance.createWorkspaceApiClient(workspaceMetadata);
        workspaceApiClient.putWorkspace(workspaceMetadata.getId(), workspace);
    }

    public void write(List<Workspace> workspaces) {
        workspaces.forEach(this::write);
    }
}
