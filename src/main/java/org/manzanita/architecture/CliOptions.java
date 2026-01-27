package org.manzanita.architecture;

import static com.google.common.collect.Sets.newHashSet;

import java.util.Optional;
import java.util.Set;
import lombok.SneakyThrows;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

public class CliOptions {

    private static final String CMD_SYSTEMS = "systems";

    private CliOptions() {
    }

    @SneakyThrows
    static Set<String> parseSystems(String[] args) {
        Options options = new Options();
        options.addOption(Option.builder("s").longOpt(CMD_SYSTEMS).hasArgs().build());
        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(options, args);
        return newHashSet(Optional.ofNullable(cmd.getOptionValues(CMD_SYSTEMS))
                .orElseGet(() -> new String[0]));
    }

}
