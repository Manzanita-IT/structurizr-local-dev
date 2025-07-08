package org.manzanita.architecture.structurizr;

import com.structurizr.dsl.StructurizrDslPlugin;
import com.structurizr.dsl.StructurizrDslPluginContext;

@SuppressWarnings("unused")
public class LandscapeEnricherPlugin implements StructurizrDslPlugin {

    @Override
    public void run(StructurizrDslPluginContext context) {
        LandscapeEnricher.from(StructurizrInstance.resolve())
                .enrich(Landscape.from(context.getWorkspace()));
    }
}
