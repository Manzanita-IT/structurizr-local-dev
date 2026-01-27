package org.manzanita.architecture.structurizr;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.manzanita.architecture.structurizr.instance.StructurizrInstance;
import com.structurizr.Workspace;
import com.structurizr.api.WorkspaceMetadata;
import com.structurizr.configuration.WorkspaceScope;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AdminApiClientTest {

    @Mock
    StructurizrInstance instance;

    @Mock
    com.structurizr.api.AdminApiClient delegate;

    AdminApiClient client;

    @BeforeEach
    void setUp() {
        client = new AdminApiClient(instance, delegate);
    }

    @Test
    void getWorkspacesRetriesAndEventuallySucceeds() throws Exception {
        when(delegate.getWorkspaces())
                .thenThrow(new RuntimeException("temporary error"))
                .thenThrow(new RuntimeException("temporary error"))
                .thenReturn(List.of());

        List<WorkspaceMetadata> result = client.getWorkspaces();

        assertNotNull(result);
        assertEquals(0, result.size());
        verify(delegate, times(3)).getWorkspaces();
    }

    @Test
    void getWorkspacesByScopeMapsAndFilters() throws Exception {
        WorkspaceMetadata m1 = mock(WorkspaceMetadata.class);
        WorkspaceMetadata m2 = mock(WorkspaceMetadata.class);
        when(delegate.getWorkspaces()).thenReturn(List.of(m1, m2));

        WorkspaceApiClient wClient1 = mock(WorkspaceApiClient.class);
        WorkspaceApiClient wClient2 = mock(WorkspaceApiClient.class);
        when(instance.createWorkspaceApiClient(m1)).thenReturn(wClient1);
        when(instance.createWorkspaceApiClient(m2)).thenReturn(wClient2);

        Workspace w1 = new Workspace("A", "");
        w1.getConfiguration().setScope(WorkspaceScope.SoftwareSystem);
        Workspace w2 = new Workspace("B", "");
        // leave w2 scope unset (null) so it will be filtered out
        when(wClient1.getWorkspace(anyLong())).thenReturn(w1);
        when(wClient2.getWorkspace(anyLong())).thenReturn(w2);

        List<Workspace> onlySystems = client.getWorkspaces(WorkspaceScope.SoftwareSystem);
        assertEquals(1, onlySystems.size());
        assertEquals("A", onlySystems.get(0).getName());

        verify(wClient1).setWorkspaceArchiveLocation(null);
        verify(wClient1).getWorkspace(anyLong());
        verify(wClient2).setWorkspaceArchiveLocation(null);
        verify(wClient2).getWorkspace(anyLong());
    }

    @Test
    void getOrCreateWorkspaceReturnsExistingWhenFound() throws Exception {
        Workspace w = new Workspace("X", "");
        WorkspaceMetadata meta = mock(WorkspaceMetadata.class);
        when(meta.getName()).thenReturn("X");
        when(delegate.getWorkspaces()).thenReturn(List.of(meta));

        WorkspaceMetadata chosen = client.getOrCreateWorkspace(w);
        assertSame(meta, chosen);
        verify(delegate, never()).createWorkspace();
    }

    @Test
    void getOrCreateWorkspaceCreatesWhenNotFound() throws Exception {
        Workspace w = new Workspace("X", "");
        when(delegate.getWorkspaces()).thenReturn(List.of());
        WorkspaceMetadata created = mock(WorkspaceMetadata.class);
        when(delegate.createWorkspace()).thenReturn(created);

        WorkspaceMetadata chosen = client.getOrCreateWorkspace(w);
        assertSame(created, chosen);
        verify(delegate).createWorkspace();
    }

    @Test
    void getWorkspaceMatchesByName() throws Exception {
        Workspace w = new Workspace("MatchMe", "");
        WorkspaceMetadata m1 = mock(WorkspaceMetadata.class);
        when(m1.getName()).thenReturn("Other");
        WorkspaceMetadata m2 = mock(WorkspaceMetadata.class);
        when(m2.getName()).thenReturn("MatchMe");
        when(delegate.getWorkspaces()).thenReturn(List.of(m1, m2));

        Optional<WorkspaceMetadata> found = client.getWorkspace(w);
        assertTrue(found.isPresent());
        assertEquals("MatchMe", found.get().getName());
    }

    @Test
    void delegatePassThroughs() throws Exception {
        client.setAgent("agent");
        verify(delegate).setAgent("agent");

        when(delegate.getAgent()).thenReturn("agent");
        assertEquals("agent", client.getAgent());

        client.deleteWorkspace(99);
        verify(delegate).deleteWorkspace(99);
    }
}
