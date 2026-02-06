package org.manzanita.architecture.structurizr;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.common.base.Joiner;
import com.structurizr.Workspace;
import com.structurizr.model.SoftwareSystem;
import java.io.File;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.Ignore;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.manzanita.architecture.structurizr.instance.LocalProperties;
import org.manzanita.architecture.structurizr.instance.LocalStructurizrInstance;

@Ignore // only needed if you want to validate written DSL
class DslIntegrationTest {

    private static LocalStructurizrInstance instance;

    @BeforeAll
    static void beforeAll() throws Exception {
        instance = LocalStructurizrInstance.start();
        int status = waitForHttp(instance.url() + "/", Duration.ofMinutes(5));
        assertTrue(status >= 200 && status < 400, "Expected HTTP OK/redirect, got: " + status);
    }

    @AfterAll
    static void afterAll() {
        instance.stop();
    }

    private static int waitForHttp(String url, Duration timeout) throws Exception {
        Instant start = Instant.now();
        Exception lastEx = null;
        int lastCode = -1;
        while (Duration.between(start, Instant.now()).compareTo(timeout) < 0) {
            try {
                HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
                conn.setInstanceFollowRedirects(false);
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);
                int code = conn.getResponseCode();
                if (code >= 200 && code < 400) {
                    return code;
                }
                lastCode = code;
            } catch (Exception e) {
                lastEx = e;
            }
            Thread.sleep(1000);
        }
        if (lastEx != null) {
            throw lastEx;
        }
        return lastCode;
    }

    @Test
    void startSpinsUpLocalStructurizrAndCreatesConfig() {
        assertNotNull(instance, "StructurizrInstance should not be null");
        assertNotNull(instance.url(), "URL should be provided");
        assertTrue(instance.url().startsWith("http://localhost:"), "URL should be instance");

        File dataDir = new File(".structurizr", "structurizr");
        assertTrue(dataDir.isDirectory(),
                "Data directory should exist: " + dataDir.getAbsolutePath());

        assertTrue(new File(dataDir, "structurizr.properties").isFile(),
                "structurizr.properties missing");
        assertTrue(new File(dataDir, "structurizr.roles").isFile(), "structurizr.roles missing");
        assertTrue(new File(dataDir, "structurizr.users").isFile(), "structurizr.users missing");
        assertTrue(new File(dataDir, "structurizr.css").isFile(), "structurizr.css missing");
        assertTrue(new File(dataDir, "structurizr.js").isFile(), "structurizr.js missing");
    }

    @Test
    void dslIsValid() {
        new WorkspaceCloner(LocalProperties.reference()).cloneTo(instance);

        assertEachWorkspaceNameMatchesSoftwareSystemName();
    }

    private void assertEachWorkspaceNameMatchesSoftwareSystemName() {
        Set<String> softwareSystemNames = softwareSystemNames();
        
        List<String> unexpected = readWorkspaces()
                .stream()
                .map(Workspace::getName)
                .filter(name -> !softwareSystemNames.contains(name))
                .toList();

        assertEquals(0, unexpected.size(),
                "Workspace and SoftwareSystem names are not corresponding: " + Joiner.on(", ")
                        .join(unexpected));
    }

    private Set<String> softwareSystemNames() {
        return new WorkspaceReader().readLandscape().getModel()
                .getSoftwareSystems().stream()
                .map(SoftwareSystem::getName)
                .collect(Collectors.toSet());
    }

    private List<Workspace> readWorkspaces() {
        return new WorkspaceReader().readSystems(Set.of()).stream()
                // Ignore the landscape workspace, because that one has no corresponding Software System
                .filter(w -> !"_Systemlandscape_".equals(w.getName()))
                .toList();
    }
}
