package searchengine.services.index;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import searchengine.model.Page;
import searchengine.model.WebSite;
import searchengine.repositories.PageRepository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.RecursiveAction;

import static java.lang.Thread.sleep;

public class HtmlParser extends RecursiveAction {
    private final String userAgent = IndexServiceImpl.components.getUserAgent();
    private final String referrer = IndexServiceImpl.components.getReferrer();
    private final PageRepository pageRepository = IndexServiceImpl.pageRepository;
    private final WebSite site;
    private final List<WebSite> pages = new ArrayList<>();

    public HtmlParser(WebSite site) {
        this.site = site;
    }

    @Override
    public void compute() {
        List<HtmlParser> tasks = new ArrayList<>();
        String url = site.getUrl();
        String path;
        String content;
        Document document = connect(url, userAgent, referrer);
        Elements elements = document.select("body").select("a");
        for (Element element : elements) {
            if (element.attr("href").startsWith("/") && !element.attr("href").endsWith("jpg")) {
                url = element.attr("abs:href");
                path = element.attr("href");
                IndexServiceImpl.webSiteRepository.changeStatusTime(site.getId());
                if (isNotVisited(site.getId(), path)) {
                    content = connect(url, userAgent, referrer).html();
                    createOnePage(content, path, site, 200);
                    WebSite newSite = new WebSite();
                    newSite.setUrl(url);
                    newSite.setId(site.getId());
                    pages.add(newSite);
                }
            }
        }

            for (WebSite pageUrl : pages) {
                HtmlParser task = new HtmlParser(pageUrl);
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

    public static Document connect(String url, String userAgent, String referrer) {
        try {
            sleep(150);
            return Jsoup.connect(url).userAgent(userAgent).referrer(referrer).ignoreHttpErrors(true).get();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public static void createOnePage(String content, String path, WebSite site, int code) {
        Page page = new Page();
        page.setContent(content);
        page.setSite(site);
        page.setPath(path);
        page.setResponseCode(code);
        IndexServiceImpl.pageRepository.save(page);
    }


}

