package org.manzanita.commons;

import java.io.File;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.catalina.connector.Connector;

@RequiredArgsConstructor
public class Tomcat {

    private final org.apache.catalina.startup.Tomcat tomcat;

    @SneakyThrows
    public void start() {
        tomcat.start();
    }

    public String url() {
        return "http://localhost:" + tomcat.getConnector().getPort();
    }

    public static Tomcat tomcat(String baseDirectory, int port, File warFile) {
        var tomcat = create(baseDirectory, port);
        deploy(warFile, tomcat);
        return new Tomcat(tomcat);
    }

    private static org.apache.catalina.startup.Tomcat create(String baseDirectory, int port) {
        var tomcat = new org.apache.catalina.startup.Tomcat();
        new File(baseDirectory).mkdirs();
        tomcat.setBaseDir(baseDirectory);
        Connector connector = new Connector("org.apache.coyote.http11.Http11NioProtocol");
        connector.setThrowOnFailure(true);
        connector.setPort(port);
        connector.setURIEncoding("UTF-8");

        tomcat.getService().addConnector(connector);
        tomcat.setConnector(connector);

        tomcat.getEngine().setBackgroundProcessorDelay(10);
        return tomcat;
    }

    private static void deploy(File warFile, org.apache.catalina.startup.Tomcat tomcat) {
        new File(tomcat.getServer().getCatalinaBase(), "webapps").mkdirs();
        tomcat.addWebapp("", warFile.getAbsolutePath());
    }
}
