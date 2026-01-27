package org.manzanita.architecture.structurizr;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.manzanita.architecture.structurizr.instance.StructurizrInstance;
import com.structurizr.Workspace;
import com.structurizr.api.WorkspaceMetadata;
import java.util.List;
import org.junit.jupiter.api.Test;

class WorkspaceWriterTest {

    @Test
    void write_Workspace_createsOrFindsAndPuts() {
        // Arrange
        StructurizrInstance instance = mock(StructurizrInstance.class);
        AdminApiClient admin = mock(AdminApiClient.class);
        when(instance.createAdminApiClient()).thenReturn(admin);

        Workspace workspace = new Workspace("Test", "");

        WorkspaceMetadata metadata = mock(WorkspaceMetadata.class);
        when(metadata.getId()).thenReturn(123);
        when(admin.getOrCreateWorkspace(workspace)).thenReturn(metadata);

        WorkspaceApiClient workspaceApiClient = mock(WorkspaceApiClient.class);
        when(instance.createWorkspaceApiClient(metadata)).thenReturn(workspaceApiClient);

        WorkspaceWriter writer = WorkspaceWriter.from(instance);

        // Act
        writer.write(workspace);

        // Assert
        verify(admin).getOrCreateWorkspace(workspace);
        verify(instance).createWorkspaceApiClient(metadata);
        verify(workspaceApiClient).putWorkspace(123L, workspace);
    }

    @Test
    void write_Landscape_delegates_to_workspace() {
        // Arrange
        StructurizrInstance instance = mock(StructurizrInstance.class);
        AdminApiClient admin = mock(AdminApiClient.class);
        when(instance.createAdminApiClient()).thenReturn(admin);

        Workspace workspace = new Workspace("Landscape", "");
        Landscape landscape = Landscape.from(workspace);

        WorkspaceMetadata metadata = mock(WorkspaceMetadata.class);
        when(metadata.getId()).thenReturn(55);
        when(admin.getOrCreateWorkspace(workspace)).thenReturn(metadata);

        WorkspaceApiClient workspaceApiClient = mock(WorkspaceApiClient.class);
        when(instance.createWorkspaceApiClient(metadata)).thenReturn(workspaceApiClient);

        WorkspaceWriter writer = WorkspaceWriter.from(instance);

        // Act
        writer.write(landscape);

        // Assert
        verify(admin).getOrCreateWorkspace(workspace);
        verify(workspaceApiClient).putWorkspace(55L, workspace);
    }

    @Test
    void write_List_calls_each_workspace() {
        // Arrange
        StructurizrInstance instance = mock(StructurizrInstance.class);
        AdminApiClient admin = mock(AdminApiClient.class);
        when(instance.createAdminApiClient()).thenReturn(admin);

        Workspace ws1 = new Workspace("WS1", "");
        Workspace ws2 = new Workspace("WS2", "");

        WorkspaceMetadata md1 = mock(WorkspaceMetadata.class);
        WorkspaceMetadata md2 = mock(WorkspaceMetadata.class);
        when(md1.getId()).thenReturn(1);
        when(md2.getId()).thenReturn(2);

        when(admin.getOrCreateWorkspace(ws1)).thenReturn(md1);
        when(admin.getOrCreateWorkspace(ws2)).thenReturn(md2);

        WorkspaceApiClient client = mock(WorkspaceApiClient.class);
        when(instance.createWorkspaceApiClient(md1)).thenReturn(client);
        when(instance.createWorkspaceApiClient(md2)).thenReturn(client);

        WorkspaceWriter writer = WorkspaceWriter.from(instance);

        // Act
        writer.write(List.of(ws1, ws2));

        // Assert
        verify(admin).getOrCreateWorkspace(ws1);
        verify(admin).getOrCreateWorkspace(ws2);
        verify(client).putWorkspace(1L, ws1);
        verify(client).putWorkspace(2L, ws2);
    }

    @Test
    void voor_uses_instance_to_create_admin_client() {
        // Arrange
        StructurizrInstance instance = mock(StructurizrInstance.class);
        AdminApiClient admin = mock(AdminApiClient.class);
        when(instance.createAdminApiClient()).thenReturn(admin);

        // Act
        WorkspaceWriter writer = WorkspaceWriter.from(instance);

        // Assert
        assertNotNull(writer);
        verify(instance, times(1)).createAdminApiClient();
    }
}
