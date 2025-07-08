package org.manzanita.architecture.structurizr.local;

import static java.lang.Integer.parseInt;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.Properties;

class LocalProperties {

    private static final Properties properties = new Properties();

    static {
        try (InputStream inputStream = LocalProperties.class.getClassLoader()
                .getResourceAsStream("local/local.properties")) {
            properties.load(inputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    static Optional<String> reference() {
        return Optional.ofNullable(properties.getProperty("reference"));
    }

    static int port() {
        return parseInt(properties.getProperty("port"));
    }

    static String version() {
        return properties.getProperty("version");
    }
}
