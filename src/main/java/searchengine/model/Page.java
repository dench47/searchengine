package searchengine.model;

import javax.persistence.*;

@Entity
public class Page {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    int id;

    @ManyToOne(cascade = CascadeType.MERGE)
    WebSite site;



    @Column(columnDefinition = "TEXT NOT NULL, UNIQUE KEY uk_path(path(500))")
//    @Column(unique = true, nullable = false)
    String path;

    @Column(nullable = false, name = "code")
    int responseCode;

    @Column(columnDefinition = "MEDIUMTEXT", nullable = true)
    String content;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public int getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(int responseCode) {
        this.responseCode = responseCode;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public WebSite getSite() {
        return site;
    }

    public void setSite(WebSite site) {
        this.site = site;
    }
}
