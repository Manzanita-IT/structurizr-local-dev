package org.manzanita.architecture;

import static com.google.common.collect.Sets.newHashSet;

import org.manzanita.architecture.structurizr.StructurizrInstance;
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
@RequiredArgsConstructor
public class Architecture {

    private static final String CMD_SYSTEMS = "systems";

    private final StructurizrInstance instance;

    private void readAndUpload(Set<String> systemNames) {
        log.info("Reading and uploading architecture...");
        WorkspaceReader reader = new WorkspaceReader();
        WorkspaceWriter writer = WorkspaceWriter.from(instance);
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

    public static void main(String[] args) {
        StructurizrInstance structurizr = StructurizrInstance.resolve();
        new Architecture(structurizr).readAndUpload(systemNames(args));
        structurizr.openInBrowser();
    }

    @SneakyThrows
    private static Set<String> systemNames(String[] args) {
        Options options = new Options();
        options.addOption(Option.builder("s").longOpt(CMD_SYSTEMS).hasArgs().build());
        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(options, args);
        return newHashSet(Optional.ofNullable(cmd.getOptionValues(CMD_SYSTEMS))
                .orElseGet(() -> new String[0]));
    }

}
