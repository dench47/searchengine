package searchengine.services.index;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
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
import java.util.concurrent.ForkJoinPool;

import static java.lang.Thread.sleep;


@Service
public class IndexServiceImpl implements IndexService {
    public static HtmlSettings components;
    public static WebSiteRepository webSiteRepository;
    public static PageRepository pageRepository;

    @Autowired
    public IndexServiceImpl(HtmlSettings components, WebSiteRepository webSiteRepository, PageRepository repository) {
        this.components = components;
        this.webSiteRepository = webSiteRepository;
        pageRepository = repository;
    }


    @Override
    public IndexResponse getIndex() {
        Site site = new Site();
        site.setName("play");
        site.setUrl("http://www.smotret.tv");
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
        try {
            sleep(150);
            Document document = Jsoup.connect(webSite.getUrl()).userAgent(components.getUserAgent()).referrer(components.getReferrer()).ignoreHttpErrors(true).get();
            HtmlParser.createOnePage(document.html(), "/", webSite, 200);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        HtmlParser parser = new HtmlParser(webSite);
        new ForkJoinPool().invoke(parser);
        webSite.setStatus(Status.INDEXED);
        webSiteRepository.changeStatusTime(webSite.getId());
    }


    private void deleteWebSiteEntity(WebSite site) {
        webSiteRepository.delete(site);
    }

    private void deletePageEntity(Page page) {
        pageRepository.delete(page);
    }


}
