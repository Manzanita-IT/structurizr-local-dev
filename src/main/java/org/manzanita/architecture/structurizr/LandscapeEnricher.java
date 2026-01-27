package org.manzanita.architecture.structurizr;

import static lombok.AccessLevel.PRIVATE;
import static org.manzanita.architecture.structurizr.WorkspaceUtils.isPersonOrSystem;
import static org.manzanita.architecture.structurizr.WorkspaceUtils.urlTo;

import com.structurizr.Workspace;
import com.structurizr.configuration.WorkspaceScope;
import com.structurizr.model.Relationship;
import com.structurizr.model.SoftwareSystem;
import com.structurizr.view.SystemLandscapeView;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.manzanita.architecture.structurizr.instance.StructurizrInstance;

@RequiredArgsConstructor(access = PRIVATE)
public class LandscapeEnricher {

    private final AdminApiClient adminApiClient;

    public static LandscapeEnricher from(StructurizrInstance instance) {
        return new LandscapeEnricher(instance.createAdminApiClient());
    }

    @SneakyThrows
    public Landscape enrich(Landscape landscape) {
        systemWorkspaces()
                .forEach(workspace -> {
                    findSystem(workspace)
                            .ifPresent(system -> enrich(landscape, system, workspace));
                    findAndCloneRelationships(workspace, landscape);
                });
        createLandscapeView(landscape);
        return landscape;
    }

    private List<Workspace> systemWorkspaces() {
        return adminApiClient.getWorkspaces(WorkspaceScope.SoftwareSystem);
    }

    private Optional<SoftwareSystem> findSystem(Workspace workspace) {
        return workspace.getModel().getSoftwareSystems().stream()
                .filter(system -> system.getName().equals(workspace.getName()))
                .findFirst();
    }

    private void enrich(Landscape landscape, SoftwareSystem system, Workspace remoteWorkspace) {
        findSystemInLandscape(landscape, system)
                .ifPresent(systemInLandscape -> {
                            systemInLandscape
                                    .setUrl(urlTo(remoteWorkspace));
                            systemInLandscape.setDescription(remoteWorkspace.getDescription());
                            systemInLandscape.addTags(system.getTags());
                        }
                );
    }

    private Optional<SoftwareSystem> findSystemInLandscape(Landscape landscape,
            SoftwareSystem system) {
        return Optional.ofNullable(
                landscape.getModel().getSoftwareSystemWithName(system.getName()));
    }

    private void findAndCloneRelationships(Workspace source, Landscape landscape) {
        for (Relationship relationship : source.getModel().getRelationships()) {
            if (isPersonOrSystem(relationship.getSource()) && isPersonOrSystem(
                    relationship.getDestination())) {
                WorkspaceUtils.cloneNonExistingRelationship(relationship, landscape.getModel());
            }
        }
    }

    private void createLandscapeView(Landscape landscape) {
        SystemLandscapeView view = landscape.getViews()
                .createSystemLandscapeView("Landscape", "Automatically generated.");
        view.addAllElements();
    }

}
