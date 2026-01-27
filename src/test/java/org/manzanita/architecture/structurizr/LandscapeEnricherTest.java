package org.manzanita.architecture.structurizr;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.manzanita.architecture.structurizr.instance.StructurizrInstance;
import com.structurizr.Workspace;
import com.structurizr.configuration.WorkspaceScope;
import com.structurizr.model.Model;
import com.structurizr.model.SoftwareSystem;
import com.structurizr.view.SystemLandscapeView;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LandscapeEnricherTest {

    @Mock
    AdminApiClient adminApiClient;

    @Test
    void enrichUpdatesSystemsClonesRelationshipsAndCreatesView() {
        // Given a Landscape with systems A and B
        Workspace workspace = new Workspace("Landscape", "");
        Landscape landscape = Landscape.from(workspace);
        Model model = landscape.getModel();
        SoftwareSystem a = model.addSoftwareSystem("A");
        SoftwareSystem b = model.addSoftwareSystem("B");

        // And remote workspaces containing A with description, tag and relation to B
        Workspace remoteA = new Workspace("A", "desc");
        remoteA.setId(100);
        SoftwareSystem remoteASys = remoteA.getModel().addSoftwareSystem("A");
        remoteASys.addTags("tag1");
        SoftwareSystem remoteBSys = remoteA.getModel().addSoftwareSystem("B");
        remoteASys.uses(remoteBSys, "uses", null, null);

        Workspace remoteZ = new Workspace("Z", "ignored");
        remoteZ.getModel().addSoftwareSystem("Z");

        when(adminApiClient.getWorkspaces(WorkspaceScope.SoftwareSystem))
                .thenReturn(List.of(remoteA, remoteZ));

        // When enriching
        StructurizrInstance instance = mock(StructurizrInstance.class);
        when(instance.createAdminApiClient()).thenReturn(adminApiClient);
        LandscapeEnricher enricher = LandscapeEnricher.from(instance);
        enricher.enrich(landscape);

        // Then system A is updated with url, description and tags
        assertEquals("{workspace:100}/diagrams#SystemContext", a.getUrl());
        assertEquals("desc", a.getDescription());
        assertTrue(a.getTags().contains("tag1"));

        // And relationship A->B exists once
        assertEquals(1, model.getRelationships().size());
        assertEquals("uses", model.getRelationships().iterator().next().getDescription());

        // And a SystemLandscape view named "Landscape" is created
        SystemLandscapeView view = landscape.getViews().getSystemLandscapeViews().stream()
                .findFirst().orElse(null);
        assertNotNull(view);
        assertEquals("Landscape", view.getKey());
        assertTrue(view.getElements().size() >= 2);

        verify(adminApiClient, atLeastOnce()).getWorkspaces(WorkspaceScope.SoftwareSystem);
    }
}
