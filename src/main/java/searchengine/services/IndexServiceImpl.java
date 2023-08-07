package searchengine.services;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.dto.indexing.HtmlParser;
import searchengine.dto.indexing.IndexResponse;
import searchengine.model.*;

import java.io.IOException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ForkJoinPool;

import static java.lang.Thread.sleep;


@Service
public class IndexServiceImpl implements IndexService {
    @Autowired
    private final WebSiteRepository webSiteRepository;
    private final SitesList sitesList;
    private final ArrayList<WebSite> webSiteEntityList;
    private final PageRepository pageRepository;


    public IndexServiceImpl(WebSiteRepository webSiteRepository, SitesList sitesList, ArrayList<WebSite> list, PageRepository repository) {
        this.webSiteRepository = webSiteRepository;
        this.sitesList = sitesList;
        this.webSiteEntityList = list;

        this.pageRepository = repository;
    }


    @Override
    public IndexResponse getIndex() {
        Site site = new Site();
        site.setName("play");
        site.setUrl("http://www.playback.ru/");
        WebSite webSite = createWebSiteEntity(site);
        createPageEntity(webSite);


//        for (WebSite webSite : webSiteEntityList) {
//            deleteWebSiteEntity(webSite);
//            if (webSite.getStatus().equals(Status.INDEXING)) {
//                return new IndexErrorResponse();
//            }
//        }
//        for (int i = 0; i < sitesList.getSites().size(); i++) {
//            Site site = sitesList.getSites().get(i);
//            WebSite newWebSiteEntity = createWebSiteEntity(site);
//            webSiteEntityList.add(newWebSiteEntity);
//        }
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
        ArrayList<Page> pageEntityList = new ArrayList<>();

        Page newPage = new Page();
//        String content = getContent(webSite);
//        newPage.setContent(content);
        newPage.setSite(webSite);
        newPage.setResponseCode(200);
        String url = webSite.getUrl();
        newPage.setPath("/");
        pageRepository.save(newPage);
        HtmlParser parser = new HtmlParser(webSite);
        new ForkJoinPool().invoke(parser);


        for (int i = 0; i < parser.getPages().size(); i++) {
            Page page = new Page();
            String path = parser.getPages().get(i).getUrl();
            page.setPath(path);
            page.setSite(webSite);
            page.setResponseCode(200);
            for (int n = 0; n <= pageEntityList.size(); n++) {

                if (!pageEntityList.contains(page)) {
                    pageRepository.save(page);


                }
                pageEntityList.add(page);
                System.out.println(page.getPath());

            }
        }


//        for (WebSite pageUrl : parser.getPages()) {
//                    pageRepository.findAll().iterator().forEachRemaining(page -> {
//                        Page nextPage = new Page();
//                        nextPage.setSite(webSite);
//                        nextPage.setResponseCode(200);
//                        nextPage.setPath(pageUrl.getUrl());
//                        if (!nextPage.equals(page)) {
//                            pageRepository.save(nextPage);
//
//                        }
//                    });


//        }
        webSite.setStatus(Status.INDEXED);
        webSiteRepository.save(webSite);
    }

    private void deleteWebSiteEntity(WebSite site) {
        webSiteRepository.delete(site);
    }

    private void deletePageEntity(Page page) {
        pageRepository.delete(page);
    }

    private String getContent(WebSite webSite) {
        String url = webSite.getUrl();
        String userAgent = "Chrome/176.59.9.133";
        try {
            sleep(150);
            Document content = Jsoup.connect(url).userAgent(userAgent).ignoreHttpErrors(true).get();
            return content.html();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void getUrls(Document document, WebSite site) {
        Elements elements = document.select("body").select("a");
        for (Element url : elements) {
            String newUrl = url.attr("abs:href");
        }
    }
}
