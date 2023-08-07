package searchengine.dto.indexing;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import searchengine.model.WebSite;
import searchengine.services.IndexServiceImpl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.RecursiveAction;

import static java.lang.Thread.sleep;

public class HtmlParser extends RecursiveAction {
    private WebSite site;
//    private String url = "http://www.playback.ru/basket.html";


    public List<WebSite> pages = new ArrayList<>();


    public HtmlParser(WebSite site) {
        this.site = site;

    }

    public List<WebSite> getPages() {
        return pages;
    }


    @Override
    public void compute() {
//        pages.add(site);
        List<HtmlParser> tasks = new ArrayList<>();
        String url = site.getUrl();
        String userAgent = "Chrome/176.59.9.133";

        try {
//            sleep(150);
            Document document = Jsoup.connect(url).userAgent(userAgent).ignoreHttpErrors(true).get();
            String content = document.html();
            Elements elements = document.select("body").select("a");

            for (Element element : elements) {
                url = element.attr("abs:href");
//                System.out.println(url);
                if (url.equals(site.getUrl())) {
                    continue;
                }
//                      (url.startsWith("http")
//                        && pages.contains(url)
                WebSite newSite = new WebSite();

                if (!url.equals(newSite.getUrl()))
                    if (!url.contains("#") && !url.contains("vk.com")) {
//                        || pages.isEmpty())
//                        System.out.println(url);
                        newSite.setUrl(url);
                        pages.add(newSite);
                    }
//                System.out.println(url);

                }

            } catch(IOException e){
                throw new RuntimeException(e);
            }

//        for (WebSite pageUrl : pages) {
////            System.out.println(pageUrl);
//            HtmlParser task = new HtmlParser(pageUrl);
//            task.fork();
//            tasks.add(task);
//        }
//
//        for (HtmlParser task : tasks) {
//            task.join();
//        }
        }

    }

