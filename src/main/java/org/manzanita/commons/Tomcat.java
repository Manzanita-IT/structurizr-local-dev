package org.manzanita.commons;

import java.io.File;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.catalina.connector.Connector;

@RequiredArgsConstructor
public class Tomcat {

    private final org.apache.catalina.startup.Tomcat tomcat;

    public static Tomcat tomcat(String baseDirectory, File warFile) {
        var tomcat = maak(baseDirectory);
        deploy(warFile, tomcat);
        return new Tomcat(tomcat);
    }

    private static org.apache.catalina.startup.Tomcat maak(String baseDirectory) {
        var tomcat = new org.apache.catalina.startup.Tomcat();
        new File(baseDirectory).mkdirs();
        tomcat.setBaseDir(baseDirectory);
        tomcat.setPort(0);
        Connector connector = new Connector("org.apache.coyote.http11.Http11NioProtocol");
        connector.setThrowOnFailure(true);
        connector.setPort(0);
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

    @SneakyThrows
    public void start() {
        tomcat.start();
    }

    @SneakyThrows
    public void stop() {
        tomcat.stop();
        tomcat.destroy();
    }

    public String url() {
        return "http://localhost:" + tomcat.getConnector().getLocalPort();
    }
}
