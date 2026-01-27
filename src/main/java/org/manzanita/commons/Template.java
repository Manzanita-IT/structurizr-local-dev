package org.manzanita.commons;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.velocity.runtime.RuntimeConstants.RESOURCE_LOADERS;
import static org.apache.velocity.runtime.RuntimeConstants.RESOURCE_LOADER_CLASS;

import java.io.File;
import java.io.FileWriter;
import java.util.Map;
import java.util.Properties;
import lombok.SneakyThrows;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;

public record Template(VelocityEngine engine) {

    @SneakyThrows
    public static void render(String input, Map<String, Object> context, File output) {
        Properties props = new Properties();
        props.setProperty(RESOURCE_LOADERS, RESOURCE_LOADER_CLASS);
        props.setProperty("resource.loader.class.class", ClasspathResourceLoader.class.getName());

        var engine = new VelocityEngine(props);
        engine.init();

        new Template(engine).doRender(input, context, output);
    }

    private void doRender(String input, Map<String, Object> context, File output) throws Exception {
        try (FileWriter writer = new FileWriter(output, UTF_8)) {
            engine.getTemplate(input, UTF_8.name())
                    .merge(new VelocityContext(context), writer);
        }
    }

}
