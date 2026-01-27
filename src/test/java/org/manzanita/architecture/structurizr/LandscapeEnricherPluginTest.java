package org.manzanita.architecture.structurizr;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import org.manzanita.architecture.structurizr.instance.StructurizrInstance;
import com.structurizr.dsl.StructurizrDslPluginContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LandscapeEnricherPluginTest {

    @Mock
    StructurizrDslPluginContext context;

    @Test
    void run_calls_enricher_with_Landscape() {
        // Arrange
        StructurizrInstance mockInstance = mock(StructurizrInstance.class);
        Landscape mockLandscape = mock(Landscape.class);
        LandscapeEnricher mockEnricher = mock(LandscapeEnricher.class);

        LandscapeEnricherPlugin plugin = new TestablePlugin(mockInstance, mockLandscape,
                mockEnricher);

        // Act
        plugin.run(context);

        // Assert
        verify(mockEnricher, times(1)).enrich(mockLandscape);
        verifyNoMoreInteractions(mockEnricher);
    }

    static class TestablePlugin extends LandscapeEnricherPlugin {

        private final StructurizrInstance instance;
        private final Landscape Landscape;
        private final LandscapeEnricher enricher;

        TestablePlugin(StructurizrInstance instance, Landscape Landscape,
                LandscapeEnricher enricher) {
            this.instance = instance;
            this.Landscape = Landscape;
            this.enricher = enricher;
        }

        @Override
        protected StructurizrInstance resolveInstance() {
            return instance;
        }

        @Override
        protected LandscapeEnricher createEnricher(StructurizrInstance instance) {
            return enricher;
        }

        @Override
        protected Landscape createLandscape(StructurizrDslPluginContext context) {
            return Landscape;
        }
    }
}
