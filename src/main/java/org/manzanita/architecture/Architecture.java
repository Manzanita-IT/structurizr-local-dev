package org.manzanita.architecture;

import static com.google.common.collect.Sets.newHashSet;
import static lombok.AccessLevel.PACKAGE;

import org.manzanita.architecture.structurizr.instance.StructurizrInstance;
import org.manzanita.architecture.structurizr.SystemLinkEnricher;
import org.manzanita.architecture.structurizr.SystemRelationshipsEnricher;
import org.manzanita.architecture.structurizr.WorkspaceReader;
import org.manzanita.architecture.structurizr.WorkspaceWriter;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

@Slf4j
@RequiredArgsConstructor(access = PACKAGE)
public class Architecture {

    private static final String CMD_SYSTEMS = "systems";

    private final StructurizrInstance instance;
    private final WorkspaceReader reader;
    private final WorkspaceWriter writer;

    static Architecture from(StructurizrInstance instance) {
        return new Architecture(instance, new WorkspaceReader(), WorkspaceWriter.from(instance));
    }

    public static void main(String[] args) {
        StructurizrInstance instance = StructurizrInstance.resolve();
        Architecture.from(instance).readAndUpload(CliOptions.parseSystems(args));
        instance.openInBrowser();
    }

    public void readAndUpload(Set<String> systemNames) {
        log.info("Reading and uploading architecture...");
        readAndEnrichSystems(reader, writer, systemNames);
        readAndEnrichLandscape(reader, writer);
        log.info("Architecture uploaded");
    }

    private void readAndEnrichSystems(WorkspaceReader reader, WorkspaceWriter writer,
            Set<String> systemNames) {
        writer.write(SystemRelationshipsEnricher
                .from(reader.readSystems(systemNames), instance)
                .enrich(systemNames));
        writer.write(SystemLinkEnricher.from(instance).enrich());
    }

    private void readAndEnrichLandscape(WorkspaceReader reader, WorkspaceWriter writer) {
        writer.write(reader.readLandscape());
    }

}
