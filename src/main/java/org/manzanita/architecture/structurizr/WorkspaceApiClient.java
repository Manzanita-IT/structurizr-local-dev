package org.manzanita.architecture.structurizr;

import com.structurizr.Workspace;
import com.structurizr.api.StructurizrClientException;
import java.io.File;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class WorkspaceApiClient {

    private final com.structurizr.api.WorkspaceApiClient delegate;

    public void setWorkspaceArchiveLocation(File workspaceArchiveLocation) {
        delegate.setWorkspaceArchiveLocation(workspaceArchiveLocation);
    }

    @SneakyThrows
    public void putWorkspace(long workspaceId, Workspace workspace) {
        try {
            delegate.putWorkspace(workspaceId, workspace);
        } catch (StructurizrClientException e) {
            if (e.getMessage().contains(
                    "The workspace could not be saved because the workspace was locked")) {
                log.warn(String.format("Workspace %d is locked, force unlocking.", workspaceId));
                delegate.unlockWorkspace(workspaceId);
                delegate.putWorkspace(workspaceId, workspace);
            } else {
                throw e;
            }
        }
    }

    @SneakyThrows
    public Workspace getWorkspace(long workspaceId) {
        return delegate.getWorkspace(workspaceId);
    }

}
