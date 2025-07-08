package org.manzanita.architecture.structurizr;

import static org.manzanita.architecture.structurizr.WorkspaceUtils.urlTo;
import static java.util.stream.Collectors.toMap;

import com.structurizr.AbstractWorkspace;
import com.structurizr.Workspace;
import com.structurizr.configuration.WorkspaceScope;
import com.structurizr.model.SoftwareSystem;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SystemLinkEnricher {

    private final Map<String, Workspace> systemenBijNaam;

    public List<Workspace> enrich() {
        return systemenBijNaam.values().stream()
                .map(this::enrich)
                .toList();
    }

    private Workspace enrich(Workspace workspace) {
        workspace
                .getModel()
                .getSoftwareSystems()
                .stream()
                .filter(s -> !s.getName().equals(workspace.getName()))
                .forEach(this::enrich);
        return workspace;
    }

    private void enrich(SoftwareSystem systeem) {
        Optional
                .ofNullable(systemenBijNaam.get(systeem.getName()))
                .ifPresent(workspace -> systeem.setUrl(urlTo(workspace)));
    }

    public static SystemLinkEnricher from(StructurizrInstance instance) {
        List<Workspace> systemen = instance.createAdminApiClient()
                .getWorkspaces(WorkspaceScope.SoftwareSystem);
        Map<String, Workspace> systemenBijNaam = systemen
                .stream()
                .collect(toMap(AbstractWorkspace::getName, Function.<Workspace>identity()));

        return new SystemLinkEnricher(systemenBijNaam);
    }

}
