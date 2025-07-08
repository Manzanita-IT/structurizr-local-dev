package org.manzanita.commons;

import java.io.File;
import java.io.FileWriter;
import java.util.Map;
import java.util.Properties;
import lombok.SneakyThrows;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;

public class Template {

    static {
        // Initialize Velocity to load templates from the classpath
        Properties velocityProps = new Properties();
        velocityProps.setProperty("resource.loaders", "class");
        velocityProps.setProperty("resource.loader.class.class",
                "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
        Velocity.init(velocityProps);
    }

    @SneakyThrows
    public static void render(String input, Map<String, Object> context, File output) {
        try (FileWriter writer = new FileWriter(output)) {
            Velocity.getTemplate(input).merge(new VelocityContext(context), writer);
        }
    }

}
