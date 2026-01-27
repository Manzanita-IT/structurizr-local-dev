package org.manzanita.architecture.structurizr;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.structurizr.Workspace;
import com.structurizr.api.StructurizrClientException;
import java.io.File;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class WorkspaceApiClientTest {

    @Mock
    com.structurizr.api.WorkspaceApiClient delegate;

    @Test
    void putWorkspaceUnlocksAndRetriesWhenLocked() throws Exception {
        WorkspaceApiClient client = new WorkspaceApiClient(delegate);
        Workspace w = new Workspace("n", "d");

        StructurizrClientException ex = mock(StructurizrClientException.class);
        when(ex.getMessage()).thenReturn(
                "The workspace could not be saved because the workspace was locked");
        doThrow(ex)
                .doNothing()
                .when(delegate).putWorkspace(42L, w);

        assertDoesNotThrow(() -> client.putWorkspace(42L, w));

        InOrder inOrder = inOrder(delegate);
        inOrder.verify(delegate).putWorkspace(42L, w);
        inOrder.verify(delegate).unlockWorkspace(42L);
        inOrder.verify(delegate).putWorkspace(42L, w);
        verifyNoMoreInteractions(delegate);
    }

    @Test
    void setWorkspaceArchiveLocationDelegates() {
        WorkspaceApiClient client = new WorkspaceApiClient(delegate);
        File f = new File("x");
        client.setWorkspaceArchiveLocation(f);
        verify(delegate).setWorkspaceArchiveLocation(f);
    }
}
