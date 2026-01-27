package org.manzanita.commons;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.nio.file.Files;
import java.util.Map;
import org.junit.jupiter.api.Test;

class TemplateTest {

    @Test
    void rendersTemplateFromClasspathToFile() throws Exception {
        File out = File.createTempFile("tpl", ".txt");
        out.deleteOnExit();
        Template.render("test/template.vm", Map.of("name", "World"), out);
        String content = Files.readString(out.toPath());
        assertEquals("Hello World", content.trim());
    }
}
