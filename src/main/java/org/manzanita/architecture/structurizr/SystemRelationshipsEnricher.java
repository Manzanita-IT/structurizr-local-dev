package org.manzanita.architecture.structurizr;

import static java.util.Comparator.comparingInt;
import static org.manzanita.architecture.structurizr.WorkspaceUtils.isPersonOrSystem;
import static org.manzanita.architecture.structurizr.WorkspaceUtils.cloneNonExistingRelationship;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;
import static lombok.AccessLevel.PRIVATE;

import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Streams;
import com.structurizr.Workspace;
import com.structurizr.configuration.WorkspaceScope;
import com.structurizr.model.Relationship;
import com.structurizr.view.SystemContextView;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.manzanita.architecture.structurizr.instance.StructurizrInstance;

@RequiredArgsConstructor(access = PRIVATE)
public class SystemRelationshipsEnricher {

    private final Map<String, Workspace> systemsByName;
    private final Multimap<String, Relationship> outgoingRelationsPerSystem;
    private final Multimap<String, Relationship> incomingRelationsPerSystem;

    public static SystemRelationshipsEnricher from(List<Workspace> localSystems, StructurizrInstance instance) {
        Map<String, Workspace> allKnownSystems = allKnownSystems(
                localSystems, instance);

        List<Relationship> relevanteRelaties = allKnownSystems.values()
                .stream()
                .flatMap(s -> s.getModel().getRelationships().stream())
                .filter(r -> isPersonOrSystem(r.getSource()) && isPersonOrSystem(
                        r.getDestination()))
                .toList();
        Multimap<String, Relationship> incomingRelationsPerSystem =
                Multimaps.index(relevanteRelaties, r -> r.getSource().getName());
        Multimap<String, Relationship> uitgaandeRelatiesPerSysteem =
                Multimaps.index(relevanteRelaties, r -> r.getDestination().getName());

        return new SystemRelationshipsEnricher(allKnownSystems, incomingRelationsPerSystem,
                uitgaandeRelatiesPerSysteem);
    }

    private static Map<String, Workspace> allKnownSystems(List<Workspace> lokaleSystemen,
            StructurizrInstance instance) {
        Map<String, Workspace> systemenBijNaam = lokaleSystemen.stream()
                .collect(toMap(Workspace::getName, identity()));

        Map<String, Workspace> remoteSystemen = instance.createAdminApiClient()
                .getWorkspaces(WorkspaceScope.SoftwareSystem)
                .stream()
                .filter(w -> !systemenBijNaam.containsKey(w.getName()))
                .collect(toMap(Workspace::getName, identity()));

        systemenBijNaam.putAll(remoteSystemen);
        return systemenBijNaam;
    }

    public List<Workspace> enrich(Set<String> systemNames) {
        return (systemNames.isEmpty() ?
                systemsByName :
                Maps.filterKeys(systemsByName, systemNames::contains))
                .values()
                .stream()
                .map(this::enrich)
                .toList();
    }

    private Workspace enrich(Workspace workspace) {
        Comparator<Relationship> higherWhenTechnologyDefined = comparingInt(r -> {
            String tech = r.getTechnology();
            return (tech == null || tech.isBlank()) ? 1 : 0; // technology defined first
        });
        List<Relationship> relationships = Streams.concat(
                relationshipsFromDifferentContexts(workspace, incomingRelationsPerSystem),
                relationshipsFromDifferentContexts(workspace, outgoingRelationsPerSystem))
                .sorted(higherWhenTechnologyDefined)
                .toList();
        relationships.forEach(r -> {
            WorkspaceUtils.cloneNonExistingRelationship(r, workspace.getModel(), "*");
        });

        workspace.getViews().getSystemContextViews().forEach(SystemContextView::addDefaultElements);
        return workspace;
    }

    private Stream<Relationship> relationshipsFromDifferentContexts(Workspace workspace,
            Multimap<String, Relationship> relationships) {
        return relationships.get(workspace.getName()).stream()
                .filter(r -> !r.getModel().equals(workspace.getModel()));
    }

}
