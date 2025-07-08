package org.manzanita.architecture.structurizr;

import static org.manzanita.commons.Retry.retry;

import com.structurizr.Workspace;
import com.structurizr.api.StructurizrClientException;
import com.structurizr.api.WorkspaceMetadata;
import com.structurizr.configuration.WorkspaceScope;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

@RequiredArgsConstructor
public class AdminApiClient {

    private final StructurizrInstance instance;
    private final com.structurizr.api.AdminApiClient delegate;

    @SneakyThrows
    public List<WorkspaceMetadata> getWorkspaces() {
        return retry(() -> {
            try {
                return delegate.getWorkspaces();
            } catch (StructurizrClientException e) {
                throw new RuntimeException(e);
            }
        })
                .maxRetries(5)
                .withDelay(1000)
                .retry();
    }

    public List<Workspace> getWorkspaces(WorkspaceScope workspaceScope) {
        return getWorkspaces()
                .stream()
                .map(metadata -> {
                    WorkspaceApiClient workspaceApiClient = instance
                            .createWorkspaceApiClient(metadata);
                    workspaceApiClient.setWorkspaceArchiveLocation(null);
                    return workspaceApiClient.getWorkspace(metadata.getId());
                })
                .filter(workspace -> workspace.getConfiguration().getScope() == workspaceScope)
                .toList();
    }

    public void setAgent(String agent) {
        delegate.setAgent(agent);
    }

    @SneakyThrows
    public void deleteWorkspace(int workspaceId) {
        delegate.deleteWorkspace(workspaceId);
    }

    @SneakyThrows
    public WorkspaceMetadata createWorkspace() {
        return delegate.createWorkspace();
    }

    public String getAgent() {
        return delegate.getAgent();
    }

    public WorkspaceMetadata getOrCreateWorkspace(Workspace workspace) {
        return getWorkspace(workspace).orElseGet(this::createWorkspace);
    }

    @SneakyThrows
    public Optional<WorkspaceMetadata> getWorkspace(Workspace workspace) {
        return getWorkspaces()
                .stream()
                .filter(m -> Optional.ofNullable(workspace.getName()).stream()
                        .allMatch(name -> name.equals(m.getName())))
                .findFirst();
    }
}
