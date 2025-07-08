package org.manzanita.architecture.structurizr;

import com.structurizr.Workspace;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

@RequiredArgsConstructor
class Scraper {

    private final String url;

    @SneakyThrows
    List<WorkspacePageObject> workspacePages() {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            URI resolve = URI.create(url).resolve("/?pageSize=" + Integer.MAX_VALUE);
            return httpClient.execute(
                    new HttpGet(resolve.toURL().toString()),
                    response -> {
                        String html = EntityUtils.toString(response.getEntity());
                        response.close();
                        Document document = Jsoup.parse(html);
                        return document.getElementsByClass("workspaceThumbnail")
                                .stream()
                                .map(e -> e.firstElementChild().attr("href"))
                                .map(x -> new WorkspacePageObject(URI.create(url).resolve(x + "/json").toString()))
                                .toList();
                    });
        }
    }

    record WorkspacePageObject(String url) {
        @SneakyThrows
        Workspace readWorkspace() {
            try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
                return httpClient.execute(new HttpGet(url),
                        response -> {
                            String json = EntityUtils.toString(response.getEntity());
                            response.close();
                            return WorkspaceUtils.fromJson(json);
                        });
            }
        }
    }

}
