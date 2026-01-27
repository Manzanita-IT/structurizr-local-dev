package org.manzanita.architecture.structurizr.instance;

import static java.util.Collections.emptyMap;
import static org.manzanita.commons.Download.download;

import java.io.File;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.manzanita.commons.Password;
import org.manzanita.commons.Template;
import org.manzanita.commons.Tomcat;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Slf4j
public record LocalStructurizrInstance(
        String url,
        String adminApiKey, Tomcat tomcat) implements StructurizrInstance {

    private static final String WORKING_DIRECTORY = ".structurizr";

    private static final String WAR_URL =
            "https://github.com/structurizr/onpremises/releases/download/v"
                    + LocalProperties.version() + "/structurizr-onpremises.war";
    private static final File WAR_OUTPUT_FILE = new File(WORKING_DIRECTORY,
            "structurizr-onpremises-v" + LocalProperties.version() + ".war");

    private static volatile LocalStructurizrInstance instance;

    public static LocalStructurizrInstance start() {
        if (instance == null) {
            synchronized (LocalStructurizrInstance.class) {
                log.info("Lokale structurizr wordt gestart...");
                maakWorkingDirectory();
                File structurizrWarFile = downloadStructurizr();
                Tomcat tomcat = Tomcat.tomcat(WORKING_DIRECTORY, structurizrWarFile);
                ApiKey apiKey = configureStructurizr();
                tomcat.start();
                instance = new LocalStructurizrInstance(tomcat.url(), apiKey.flat, tomcat);
                log.info("Lokale Structurizr gestart");
            }
        }
        return instance;
    }

    private static void maakWorkingDirectory() {
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

    private static ApiKey configureStructurizr() {
        File dataDirectory = new File(WORKING_DIRECTORY, "structurizr");
        //noinspection ResultOfMethodCallIgnored
        dataDirectory.mkdirs();
        System.setProperty("structurizr.dataDirectory", dataDirectory.getAbsolutePath());

        ApiKey apiKey = ApiKey.create();
        configureProperties(dataDirectory, apiKey);
        configureUsers(dataDirectory);
        configureStyling(dataDirectory);
        return apiKey;
    }

    private static void configureProperties(File dataDirectory, ApiKey apiKey) {
        Template.render(
                "local/structurizr.properties.vm",
                Map.of("structurizrApiKey", apiKey.bcrypted),
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

    @Override
    public void stop() {
        tomcat.stop();
    }

    private record ApiKey(String flat, String bcrypted) {

        private static ApiKey create() {
            String apiKey = Password.generate();
            return new ApiKey(apiKey,
                    new BCryptPasswordEncoder().encode(apiKey));
        }
    }
}
