package org.manzanita.architecture.structurizr;

import org.manzanita.architecture.structurizr.Scraper.WorkspacePageObject;
import com.structurizr.Workspace;
import com.structurizr.api.WorkspaceMetadata;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.manzanita.architecture.structurizr.instance.StructurizrInstance;

/**
 * This class allows cloning workspaces between Structurizr instances in an unauthenticated way.
 */
@RequiredArgsConstructor
@Slf4j
public class WorkspaceCloner {

    private final String sourceUrl;

    @SneakyThrows
    public void cloneTo(StructurizrInstance target) {
        log.info("Cloning {} to {}...", sourceUrl, target.url());
        AdminApiClient targetAdminApi = target.createAdminApiClient();

        readWorkspacesFrom(sourceUrl)
                .forEach(sourceWorkspace -> {
                    if (targetAdminApi.getWorkspace(sourceWorkspace).isEmpty()) {
                        WorkspaceMetadata targetWorkspace = targetAdminApi.createWorkspace();
                        sourceWorkspace.setId(targetWorkspace.getId());
                        WorkspaceApiClient targetWorkspaceApi = target.createWorkspaceApiClient(
                                targetWorkspace);
                        targetWorkspaceApi.putWorkspace(targetWorkspace.getId(), sourceWorkspace);
                    }
                });
        log.info("Cloning {} to {} successful.", sourceUrl, target.url());
    }

    private Stream<Workspace> readWorkspacesFrom(String sourceUrl) {
        return new Scraper(sourceUrl).workspacePages().stream().
                map(WorkspacePageObject::readWorkspace);
    }
}
