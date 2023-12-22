package searchengine.services.index;

import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import searchengine.config.HtmlSettings;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.dto.indexing.IndexErrorResponse;
import searchengine.dto.indexing.IndexResponse;
import searchengine.dto.indexing.StopIndexingErrorResponse;
import searchengine.model.Page;
import searchengine.model.Status;
import searchengine.model.WebSite;
import searchengine.repositories.PageRepository;
import searchengine.repositories.WebSiteRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ForkJoinPool;


@Service
public class IndexServiceImpl implements IndexService {
    public boolean stop;
    public static Thread thread;

    public static boolean isIndexing;
    protected static HtmlSettings components;
    protected SitesList sitesList;
    protected static WebSiteRepository webSiteRepository;
    protected static PageRepository pageRepository;

    @Autowired
    public IndexServiceImpl(HtmlSettings components, WebSiteRepository webSiteRepository, PageRepository repository, SitesList sitesList) {
        this.components = components;
        this.webSiteRepository = webSiteRepository;
        pageRepository = repository;
        this.sitesList = sitesList;
    }


    public IndexResponse getIndex() {
        if (!isIndexing) {
            List<Site> sites = sitesList.getSites();
            for (Site site : sites) {
                thread(site);

                thread.start();
//                thread.interrupt();
                    try {
                        thread.join();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }

            }

            IndexResponse response = new IndexResponse();
            response.setResult(true);
            return response;
        }
        return new IndexErrorResponse();
    }

//    @Override
//    public IndexResponse getIndex() {
//        if (!isIndexing) {
//            isIndexing = true;
//            List<Site> sites = sitesList.getSites();
//            List<WebSite> webSiteList = webSiteRepository.findAll();
//            List<Page> pageList = pageRepository.findAll();
//            for (Site site : sites) {
//                if (webSiteList.size() != 0) {
//                    for (WebSite webSite : webSiteList) {
//                        for (Page page : pageList) {
//                            if (pageRepository.existsBySiteIdAndPath(webSite.getId(), page.getPath()) &&
//                                    webSite.getUrl().equals(site.getUrl())) {
//                                deletePageEntity(page);
//                            }
//                        }
//                        if (webSite.getUrl().equals(site.getUrl())) {
//                            webSiteRepository.deleteSiteFromDb(site.getUrl());
//                        }
//                    }
//                }
//                WebSite webSite = createWebSiteEntity(site);
//                createPageEntity(webSite);
//            }
//            isIndexing = false;
//            IndexResponse response = new IndexResponse();
//            response.setResult(true);
//            return response;
//        }
//        isIndexing = false;
//        return new IndexErrorResponse();
//    }


    private void thread(Site site) {
        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                isIndexing = true;
                List<WebSite> webSiteList = webSiteRepository.findAll();
                List<Page> pageList = pageRepository.findAll();
                if (webSiteList.size() != 0) {
                    for (WebSite webSite : webSiteList) {
                        for (Page page : pageList) {
                            if (Thread.currentThread().isInterrupted()) {
                                break;
                            }
                            if (pageRepository.existsBySiteIdAndPath(webSite.getId(), page.getPath()) &&
                                    webSite.getUrl().equals(site.getUrl())) {
                                deletePageEntity(page);
                            }
                        }
                        if (webSite.getUrl().equals(site.getUrl())) {
                            webSiteRepository.deleteSiteFromDb(site.getUrl());
                        }
                    }
                }
                WebSite webSite = createWebSiteEntity(site);
                createPageEntity(webSite);
            }
        });
    }


    @Override
    public IndexResponse stopIndex() {
//        thread.interrupt();
        HtmlParser.current.interrupt();
        System.out.println(HtmlParser.current.getName());
//        Thread.currentThread().interrupt();
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
        Document document = HtmlParser.connect(webSite.getUrl(), components.getUserAgent(), components.getReferrer());
        HtmlParser.createOnePage(document.html(), webSite.getUrl(), webSite, 200);
        HtmlParser parser = new HtmlParser(webSite);
        new ForkJoinPool().invoke(parser);
        webSite.setStatus(Status.INDEXED);
//        webSiteRepository.changeStatus(webSite.getId());
        webSiteRepository.changeStatusTime(webSite.getId());
    }

    private void deleteWebSiteEntity(WebSite site) {
        webSiteRepository.delete(site);
    }

    private void deletePageEntity(Page page) {
        pageRepository.delete(page);

    }


}
