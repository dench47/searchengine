package searchengine.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import searchengine.config.Site;
import searchengine.dto.indexing.IndexResponse;
import searchengine.model.Page;
import searchengine.model.Status;
import searchengine.model.WebSite;
import searchengine.repositories.PageRepository;
import searchengine.repositories.WebSiteRepository;

import java.time.LocalDateTime;
import java.util.concurrent.ForkJoinPool;


@Service
public class IndexServiceImpl implements IndexService {

    private final WebSiteRepository webSiteRepository;
    private final PageRepository pageRepository;

    @Autowired
    public IndexServiceImpl(WebSiteRepository webSiteRepository, PageRepository repository) {
        this.webSiteRepository = webSiteRepository;
        this.pageRepository = repository;
    }


    @Override
    public IndexResponse getIndex() {

//        pageRepository.deleteAll();
//        webSiteRepository.deleteAll();
        Site site = new Site();
        site.setName("play");
        site.setUrl("http://www.lenta.ru/");
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
        HtmlParser parser = new HtmlParser(webSiteRepository, pageRepository, webSite);
        new ForkJoinPool().invoke(parser);
        webSite.setStatus(Status.INDEXED);
        webSiteRepository.save(webSite);
    }

    private void deleteWebSiteEntity(WebSite site) {
        webSiteRepository.delete(site);
    }

    private void deletePageEntity(Page page) {
        pageRepository.delete(page);
    }


}
