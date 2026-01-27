package org.manzanita.architecture.structurizr;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.structurizr.Workspace;
import com.structurizr.model.SoftwareSystem;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class SystemLinkEnricherTest {

    @Test
    void enrich_setsUrlForKnownForeignSystems_andDoesNotTouchOwnSystem() {
        // Arrange: two workspaces representing systems A and B
        Workspace wA = new Workspace("A", "");
        wA.setId(111);
        Workspace wB = new Workspace("B", "");
        wB.setId(222);

        // The model of workspace A references its own system A and foreign system B
        SoftwareSystem aInA = wA.getModel().addSoftwareSystem("A");
        SoftwareSystem bInA = wA.getModel().addSoftwareSystem("B");
        assertNull(aInA.getUrl());
        assertNull(bInA.getUrl());

        Map<String, Workspace> systemenBijNaam = new HashMap<>();
        systemenBijNaam.put("A", wA);
        systemenBijNaam.put("B", wB);

        SystemLinkEnricher enricher = new SystemLinkEnricher(systemenBijNaam);

        // Act
        List<Workspace> result = enricher.enrich();

        // Assert
        assertEquals(2, result.size());
        assertNull(aInA.getUrl(), "Own system should not receive a URL");
        assertEquals("{workspace:222}/diagrams#SystemContext", bInA.getUrl(),
                "Foreign system should link to its workspace SystemContext view");
    }

    @Test
    void enrich_ignoresUnknownForeignSystems() {
        Workspace wA = new Workspace("A", "");
        wA.setId(111);
        wA.getModel().addSoftwareSystem("A");
        SoftwareSystem unknownInA = wA.getModel().addSoftwareSystem("UNKNOWN");

        Map<String, Workspace> systemenBijNaam = Map.of("A", wA);
        SystemLinkEnricher enricher = new SystemLinkEnricher(systemenBijNaam);

        // Act
        enricher.enrich();

        // Assert: No URL set because UNKNOWN is not present in the map
        assertNull(unknownInA.getUrl());
    }
}
