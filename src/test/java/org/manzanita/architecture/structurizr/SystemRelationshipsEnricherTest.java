package org.manzanita.architecture.structurizr;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.structurizr.Workspace;
import com.structurizr.model.Model;
import com.structurizr.model.Person;
import com.structurizr.model.Relationship;
import com.structurizr.model.SoftwareSystem;
import com.structurizr.view.SystemContextView;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

class SystemRelationshipsEnricherTest {

    @Test
    void enrich_clonesRelationshipsFromOtherWorkspaces_andUpdatesSystemContextView()
            throws Exception {
        // Workspaces A and B
        Workspace wA = new Workspace("A", "");
        Workspace wB = new Workspace("B", "");

        // Models contain persons and systems with same names across workspaces
        Model mA = wA.getModel();
        SoftwareSystem a_A = mA.addSoftwareSystem("A");
        SoftwareSystem b_A = mA.addSoftwareSystem("B");
        Person p_A = mA.addPerson("P");
        // Relationships in A (same model): A->B, P->A
        Relationship aToB_inA = a_A.uses(b_A, "calls", null, null);
        Relationship pToA_inA = p_A.uses(a_A, "uses", null, null);

        Model mB = wB.getModel();
        SoftwareSystem a_B = mB.addSoftwareSystem("A");
        SoftwareSystem b_B = mB.addSoftwareSystem("B");
        Person p_B = mB.addPerson("P");
        // Relationships in B (other model): B->A, A->P
        Relationship bToA_inB = b_B.uses(a_B, "replies", null, null);
        Relationship aToP_inB = a_B.uses(p_B, "notifies", null, null);

        // System context view for A to check addDefaultElements effect later
        SystemContextView viewA = wA.getViews()
                .createSystemContextView(a_A, "A-context", "context");
        int initialViewElementCount = viewA.getElements().size();

        // Build alleGekendeSystemen
        Map<String, Workspace> systemenBijNaam = new HashMap<>();
        systemenBijNaam.put("A", wA);
        systemenBijNaam.put("B", wB);

        // Relevant relationships across all known systems
        List<Relationship> relevanteRelaties = new ArrayList<>();
        relevanteRelaties.add(aToB_inA);
        relevanteRelaties.add(pToA_inA);
        relevanteRelaties.add(bToA_inB);
        relevanteRelaties.add(aToP_inB);

        Multimap<String, Relationship> inkomendeRelatiesPerSysteem =
                Multimaps.index(relevanteRelaties, r -> r.getSource().getName());
        Multimap<String, Relationship> uitgaandeRelatiesPerSysteem =
                Multimaps.index(relevanteRelaties, r -> r.getDestination().getName());

        // Construct SystemRelationshipsEnricher via reflection (private constructor)
        Constructor<SystemRelationshipsEnricher> ctor = SystemRelationshipsEnricher.class
                .getDeclaredConstructor(Map.class, Multimap.class, Multimap.class);
        ctor.setAccessible(true);
        SystemRelationshipsEnricher enricher = ctor.newInstance(systemenBijNaam,
                uitgaandeRelatiesPerSysteem, inkomendeRelatiesPerSysteem);

        // Act: enrich only workspace A
        List<Workspace> result = enricher.enrich(Set.of("A"));

        // Assert result includes A only
        assertEquals(1, result.size());
        assertSame(wA, result.get(0));

        // From other model, for key "A": we should clone B->A and A->P into model A, with suffix "*"
        // Existing relationships in A model were 2; after cloning 2 more expected
        assertEquals(4, mA.getRelationships().size());
        boolean hasBtoAClone = mA.getRelationships().stream()
                .anyMatch(r -> r.getSource().getName().equals("B")
                        && r.getDestination().getName().equals("A")
                        && r.getDescription().equals("replies*"));
        boolean hasAtoPClone = mA.getRelationships().stream()
                .anyMatch(r -> r.getSource().getName().equals("A")
                        && r.getDestination().getName().equals("P")
                        && r.getDescription().equals("notifies*"));
        assertTrue(hasBtoAClone, "Expected cloned relationship B->A with '*'");
        assertTrue(hasAtoPClone, "Expected cloned relationship A->P with '*'");

        // View should have default elements added
        assertTrue(viewA.getElements().size() > initialViewElementCount,
                "SystemContextView should have more elements after enrichment");
    }
}
