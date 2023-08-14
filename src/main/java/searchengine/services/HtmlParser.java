package searchengine.services;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import searchengine.model.Page;
import searchengine.model.WebSite;
import searchengine.repositories.PageRepository;
import searchengine.repositories.WebSiteRepository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.RecursiveAction;

import static java.lang.Thread.sleep;

public class HtmlParser extends RecursiveAction {

    protected final WebSiteRepository webSiteRepository;
    protected final PageRepository pageRepository;
    protected WebSite newPage;
    private List<WebSite> pages = new ArrayList<>();

    @Autowired
    public HtmlParser(WebSiteRepository webSiteRepository, PageRepository pageRepository, WebSite newPage) {
        this.webSiteRepository = webSiteRepository;
        this.pageRepository = pageRepository;
        this.newPage = newPage;
    }

    @Override
    public void compute() {
        List<HtmlParser> tasks = new ArrayList<>();

        String url = newPage.getUrl();
        String path;
        String userAgent = "Chrome/176.59.9.133";
        try {
            sleep(150);
            Document document = Jsoup.connect(url).userAgent(userAgent).ignoreHttpErrors(true).get();
            String content = document.html();
            Elements elements = document.select("body").select("a");
            for (Element element : elements) {
                if (element.attr("href").startsWith("/") && !element.attr("href").endsWith("jpg")) {
                    url = element.attr("abs:href");
                    path = element.attr("href");
                    if (isNotVisited(newPage.getId(), path)) {
                    Page page = new Page();
                    page.setSite(newPage);
                    page.setPath(path);
                    page.setResponseCode(200);
                        pageRepository.save(page);
                        WebSite newSite = new WebSite();
                        newSite.setUrl(url);
                        newSite.setId(newPage.getId());
                        pages.add(newSite);
                    }
                }
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        for (WebSite pageUrl : pages) {
            HtmlParser task = new HtmlParser(webSiteRepository, pageRepository, pageUrl);
            task.fork();
            tasks.add(task);
        }

        for (HtmlParser task : tasks) {
            task.join();
        }
    }

    private boolean isNotVisited(Integer id, String path) {
        return !pageRepository.existsBySiteIdAndPath(id, path);
    }


}

