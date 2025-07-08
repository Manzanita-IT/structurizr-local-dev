package org.manzanita.architecture.structurizr.local;

import static java.util.Collections.emptyMap;
import static org.manzanita.commons.Download.download;
import static org.manzanita.commons.Tomcat.tomcat;

import java.io.File;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.manzanita.architecture.structurizr.StructurizrInstance;
import org.manzanita.architecture.structurizr.WorkspaceCloner;
import org.manzanita.commons.Password;
import org.manzanita.commons.Template;
import org.manzanita.commons.Tomcat;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Slf4j
public record LocalStructurizr(String url, String apiKey) {

    private static final String WORKING_DIRECTORY = ".structurizr";

    private static final String WAR_URL =
            "https://github.com/structurizr/onpremises/releases/download/v"
                    + LocalProperties.version() + "/structurizr-onpremises.war";
    private static final File WAR_OUTPUT_FILE = new File(WORKING_DIRECTORY,
            "/structurizr-onpremises-v" + LocalProperties.version() + ".war");

    private static volatile StructurizrInstance instance;

    public static StructurizrInstance start() {
        if (instance == null) {
            synchronized (LocalStructurizr.class) {
                log.info("Local Structurizr starting...");
                createWorkingDirectory();
                File structurizrWarFile = downloadStructurizr();
                Tomcat tomcat = tomcat(WORKING_DIRECTORY, LocalProperties.port(),
                        structurizrWarFile);
                ApiKey apiKey = configureStructurizr(tomcat.url());
                tomcat.start();
                instance = new StructurizrInstance(tomcat.url(), apiKey.flat);
                LocalProperties.reference()
                        .ifPresent(reference -> new WorkspaceCloner(reference).cloneTo(instance));
                log.info("Local Structurizr started");
            }
        }
        return instance;
    }

    private static void createWorkingDirectory() {
        //noinspection ResultOfMethodCallIgnored
        new File(WORKING_DIRECTORY).mkdirs();
    }

    private static File downloadStructurizr() {
        File warFile = WAR_OUTPUT_FILE;
        if (!WAR_OUTPUT_FILE.exists()) {
            download(WAR_URL, warFile);
        }
        return warFile;
    }

    private static ApiKey configureStructurizr(String url) {
        File dataDirectory = new File(WORKING_DIRECTORY, "structurizr");
        //noinspection ResultOfMethodCallIgnored
        dataDirectory.mkdirs();
        System.setProperty("structurizr.dataDirectory", dataDirectory.getAbsolutePath());

        ApiKey apiKey = ApiKey.create();
        configureProperties(dataDirectory, url, apiKey);
        configureUsers(dataDirectory);
        configureStyling(dataDirectory);
        return apiKey;
    }

    private static void configureProperties(File dataDirectory, String url, ApiKey apiKey) {
        Template.render(
                "local/structurizr.properties.vm",
                Map.of(
                        "structurizrUrl", url,
                        "structurizrApiKey", apiKey.bcrypted),
                new File(dataDirectory, "structurizr.properties"));
    }

    private static void configureUsers(File dataDirectory) {
        Template.render(
                "local/structurizr.roles.vm",
                emptyMap(),
                new File(dataDirectory, "structurizr.roles"));
        Template.render(
                "local/structurizr.users.vm",
                emptyMap(),
                new File(dataDirectory, "structurizr.users"));
    }

    private static void configureStyling(File dataDirectory) {
        Template.render(
                "local/structurizr.css.vm",
                emptyMap(),
                new File(dataDirectory, "structurizr.css"));
        Template.render(
                "local/structurizr.js.vm",
                emptyMap(),
                new File(dataDirectory, "structurizr.js"));
    }

    private record ApiKey(String flat, String bcrypted) {

        private static ApiKey create() {
            String apiKey = Password.genereer();
            return new ApiKey(apiKey,
                    new BCryptPasswordEncoder().encode(apiKey));
        }
    }

}
