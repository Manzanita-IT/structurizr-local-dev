package org.manzanita.architecture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.same;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import org.manzanita.architecture.structurizr.AdminApiClient;
import org.manzanita.architecture.structurizr.Landscape;
import org.manzanita.architecture.structurizr.WorkspaceWriter;
import org.manzanita.architecture.structurizr.instance.StructurizrInstance;
import org.manzanita.architecture.structurizr.WorkspaceReader;
import com.structurizr.Workspace;
import com.structurizr.configuration.WorkspaceScope;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ArchitectureTest {

    @Mock
    StructurizrInstance instance;

    @Mock
    AdminApiClient adminApiClient;

    @Mock
    WorkspaceReader reader;

    @Mock
    WorkspaceWriter writer;

    @Test
    void systemenOptionParsesOptions() {
        Set<String> empty = CliOptions.parseSystems(new String[]{});
        assertTrue(empty.isEmpty());

        Set<String> names = CliOptions.parseSystems(new String[]{"-s", "a", "b", "--systems", "c"});
        assertEquals(Set.of("a", "b", "c"), names);
    }

    @Test
    void leesEnUploadInvokesLezerAndWriterInOrder() {
        // Given
        when(instance.createAdminApiClient()).thenReturn(adminApiClient);
        when(adminApiClient.getWorkspaces(WorkspaceScope.SoftwareSystem)).thenReturn(List.of());

        Workspace wsA = new Workspace("A", "");
        when(reader.readSystems(anySet())).thenReturn(List.of(wsA));
        // Return a mock Landscape to avoid package visibility on factory
        Landscape mockedLandscape = mock(Landscape.class);
        when(reader.readLandscape()).thenReturn(mockedLandscape);

        Architecture arch = new Architecture(instance, reader, writer);

        // When
        Set<String> systems = Set.of("A");
        arch.readAndUpload(systems);

        // Then
        verify(reader, times(1)).readSystems(same(systems));
        // Systems enrichment writes twice (relations + links)
        verify(writer, times(2)).write(any(List.class));
        // Landscape is written once
        verify(reader, times(1)).readLandscape();
        verify(writer, times(1)).write(same(mockedLandscape));

        verifyNoMoreInteractions(writer);
    }
}
