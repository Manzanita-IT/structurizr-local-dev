package org.manzanita.architecture.structurizr.instance;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public record RemoteStructurizrInstance(
        String url,
        String adminApiKey) implements StructurizrInstance {

    @Override
    public void stop() {
        throw new SecurityException("Not allowed to stop remote instance.");
    }
}
