package searchengine.services;

import ch.qos.logback.classic.joran.action.ReceiverAction;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import searchengine.config.HtmlSettings;
import searchengine.config.Site;
import searchengine.dto.indexing.IndexResponse;
import searchengine.model.Page;
import searchengine.model.Status;
import searchengine.model.WebSite;
import searchengine.repositories.PageRepository;
import searchengine.repositories.WebSiteRepository;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

import static java.lang.Thread.sleep;


@Service
public class IndexServiceImpl implements IndexService {
    int count = 0;
    private final HtmlSettings components;

    private final WebSiteRepository webSiteRepository;
    private final PageRepository pageRepository;
//    private List<WebSite> newWebSites = new ArrayList<>();

    @Autowired
    public IndexServiceImpl(HtmlSettings components, WebSiteRepository webSiteRepository, PageRepository repository) {
        this.components = components;
        this.webSiteRepository = webSiteRepository;
        this.pageRepository = repository;
    }


    @Override
    public IndexResponse getIndex() {
//        pageRepository.deleteAll();
//        webSiteRepository.deleteAll();
        Site site = new Site();
        site.setName("play");
        site.setUrl("http://www.lenta.ru");
        WebSite webSite = createWebSiteEntity(site);
        createPageEntity(webSite);
        IndexResponse response = new IndexResponse();
        response.setResult(true);
        return response;
    }


    private WebSite createWebSiteEntity(Site site) {
        WebSite newSite = new WebSite();
        newSite.setLastError("");
        newSite.setName(site.getName());
        newSite.setUrl(site.getUrl());
        newSite.setStatus(Status.INDEXING);
        newSite.setStatusTime(LocalDateTime.now());
        webSiteRepository.save(newSite);
        return newSite;
    }

    private void createPageEntity(WebSite webSite) {
//        HtmlParser parser = new HtmlParser(components, webSiteRepository, pageRepository, webSite);
        Experiment experiment = new Experiment(webSite);
        new ForkJoinPool().invoke(experiment);
        webSite.setStatus(Status.INDEXED);
        webSiteRepository.save(webSite);
    }


    public class Experiment extends RecursiveAction {
        private WebSite site;
        private List<WebSite> newWebSites = new ArrayList<>();


        public Experiment(WebSite site) {
            this.site = site;
        }

        @Override
        protected void compute() {
            List<Experiment> tasks = new ArrayList<>();
            String url = site.getUrl();
            String path;
            String content;
            try {
                sleep(150);
                Document document = Jsoup.connect(url).userAgent(components.getUserAgent()).ignoreHttpErrors(true).get();

                content = document.html();

                Elements elements = document.select("body").select("a");
                for (Element element : elements) {
                    if (element.attr("href").startsWith("/") && !element.attr("href").endsWith("jpg")) {
                        url = element.attr("abs:href");
                        path = element.attr("href");
                        if (isNotVisited(site.getId(), path)) {

                            Page page = new Page();
                            page.setResponseCode(200);
                            page.setPath(path);
                            page.setSite(site);
//                            page.setContent(content);
                            pageRepository.save(page);


//                            WebSite newSite = new WebSite();
//                            newSite.setUrl(url);
//                            newSite.setId(site.getId());
//                            newWebSites.add(newSite);
                            site.setUrl(url);
                            newWebSites.add(site);

                        }
                        count += 1;
                        System.out.println(count);
                    }
                }

            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }

            for (WebSite site1 : newWebSites) {
                Experiment task = new Experiment(site1);
                task.fork();
                tasks.add(task);
            }

            for (Experiment task : tasks) {
                task.join();
            }

        }
    }


    private void deleteWebSiteEntity(WebSite site) {
        webSiteRepository.delete(site);
    }

    private void deletePageEntity(Page page) {
        pageRepository.delete(page);
    }


    private boolean isNotVisited(Integer id, String path) {
        return !pageRepository.existsBySiteIdAndPath(id, path);
    }


}
