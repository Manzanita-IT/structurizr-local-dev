package org.manzanita.architecture.structurizr;

import com.structurizr.dsl.StructurizrDslPlugin;
import com.structurizr.dsl.StructurizrDslPluginContext;
import org.manzanita.architecture.structurizr.instance.StructurizrInstance;

@SuppressWarnings("unused") // Used from dsl/_landscape_/C0_enriched_landscape.dsl
public class LandscapeEnricherPlugin implements StructurizrDslPlugin {

    @Override
    public void run(StructurizrDslPluginContext context) {
        StructurizrInstance instance = StructurizrInstance.resolve();
        createEnricher(instance)
                .enrich(createLandscape(context));
    }

    // Test seam methods to ease unit testing without static mocking
    protected StructurizrInstance resolveInstance() {
        return StructurizrInstance.resolve();
    }

    protected LandscapeEnricher createEnricher(StructurizrInstance instance) {
        return LandscapeEnricher.from(instance);
    }

    protected Landscape createLandscape(StructurizrDslPluginContext context) {
        return Landscape.from(context.getWorkspace());
    }

}
