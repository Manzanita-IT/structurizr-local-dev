package org.manzanita.architecture.structurizr;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.structurizr.Workspace;
import com.structurizr.model.Container;
import com.structurizr.model.Model;
import com.structurizr.model.Person;
import com.structurizr.model.Relationship;
import com.structurizr.model.SoftwareSystem;
import org.junit.jupiter.api.Test;

class WorkspaceUtilsTest {

    @Test
    void isPersonOrSystemWorks() {
        Workspace w = new Workspace("w", "");
        Model m = w.getModel();
        Person p = m.addPerson("P");
        SoftwareSystem s = m.addSoftwareSystem("S");
        Container c = s.addContainer("C");

        assertTrue(WorkspaceUtils.isPersonOrSystem(p));
        assertTrue(WorkspaceUtils.isPersonOrSystem(s));
        assertFalse(WorkspaceUtils.isPersonOrSystem(c));
    }

    @Test
    void kloonOnbestaandeRelatieClonesOnceAndAddsSuffix() {
        Workspace src = new Workspace("src", "");
        Model srcModel = src.getModel();
        SoftwareSystem a = srcModel.addSoftwareSystem("A");
        SoftwareSystem b = srcModel.addSoftwareSystem("B");
        Relationship rel = a.uses(b, "calls", null, null);

        Workspace dst = new Workspace("dst", "");
        Model dstModel = dst.getModel();
        dstModel.addSoftwareSystem("A");
        dstModel.addSoftwareSystem("B");

        // First clone
        WorkspaceUtils.cloneNonExistingRelationship(rel, dstModel);
        assertEquals(1, dstModel.getRelationships().size());
        assertEquals("calls", dstModel.getRelationships().iterator().next().getDescription());

        // Second clone should not duplicate
        WorkspaceUtils.cloneNonExistingRelationship(rel, dstModel);
        assertEquals(1, dstModel.getRelationships().size());

        // Clone with suffix into fresh model
        Workspace dst2 = new Workspace("dst2", "");
        Model dstModel2 = dst2.getModel();
        dstModel2.addSoftwareSystem("A");
        dstModel2.addSoftwareSystem("B");
        WorkspaceUtils.cloneNonExistingRelationship(rel, dstModel2, "-copy");
        assertEquals("calls-copy", dstModel2.getRelationships().iterator().next().getDescription());
    }

    @Test
    void urlNaarFormatsCorrectly() {
        Workspace w = new Workspace("w", "");
        w.setId(1234);
        assertEquals("{workspace:1234}/diagrams#SystemContext", WorkspaceUtils.urlTo(w));
    }

    @Test
    void fromJsonReadsWorkspace() throws Exception {
        Workspace w = new Workspace("name", "desc");
        String json = com.structurizr.util.WorkspaceUtils.toJson(w, false);
        Workspace read = WorkspaceUtils.fromJson(json);
        assertEquals("name", read.getName());
    }
}
