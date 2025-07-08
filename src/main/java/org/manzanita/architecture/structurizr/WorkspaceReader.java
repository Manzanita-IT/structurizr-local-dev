package org.manzanita.architecture.structurizr;

import com.structurizr.Workspace;
import com.structurizr.dsl.StructurizrDslParser;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import lombok.SneakyThrows;

public class WorkspaceReader {

    private static final Path DSL_PATH = Path.of("dsl");
    private static final String CONTEXT_DSL = "C1_context.dsl";
    private static final String CONTAINER_DSL = "C2_container.dsl";
    private static final String COMPONENT_DSL = "C3_component.dsl";
    private static final String LANDSCAPE_DSL = "_landscape_/C0_enriched_landscape.dsl";

    @SneakyThrows
    public List<Workspace> readSystems(Set<String> systemNames) {
        try (Stream<Path> files = Files.list(DSL_PATH)) {
            return files
                    .map(Path::toFile)
                    .filter(File::isDirectory)
                    .flatMap(this::deepestLevel)
                    .map(this::read)
                    .filter(w -> systemNames.isEmpty() || systemNames.contains(w.getName()))
                    .toList();
        }
    }

    private Stream<File> deepestLevel(File directory) {
        return readLevel(directory, COMPONENT_DSL)
                .or(() -> readLevel(directory, CONTAINER_DSL))
                .or(() -> readLevel(directory, CONTEXT_DSL)).stream();
    }

    private Optional<File> readLevel(File directory, String level) {
        File file = Path.of(directory.toURI()).resolve(level).toFile();
        return file.exists() ? Optional.of(file) : Optional.empty();
    }

    @SneakyThrows
    public Landscape readLandscape() {
        return Landscape.from(read(DSL_PATH.resolve(LANDSCAPE_DSL).toFile()));
    }

    @SneakyThrows
    private Workspace read(File file) {
        StructurizrDslParser parser = new StructurizrDslParser();
        parser.parse(file);
        return parser.getWorkspace();
    }

}
