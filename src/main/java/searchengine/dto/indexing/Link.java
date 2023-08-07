package searchengine.dto.indexing;

import java.util.ArrayList;

public class Link {
    private String name;
    private Link parent;
    private ArrayList<Link> links = new ArrayList<>();

    public Link(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public ArrayList<Link> getLinks() {
        return links;
    }

    private Link getRoot() {
        if (parent == null) {
            return this;
        }
        return parent.getRoot();
    }

    public void setParent(Link parent) {
        this.parent = parent;
    }

    private boolean linkAlreadyExists(String linkName) {
        if (this.name.equals(linkName)) {
            return true;
        }
        for (Link link : links) {
            if (link.linkAlreadyExists(linkName)) {
                return true;
            }
        }
        return false;
    }

    public void addLink(Link link) {
       links.add(link);
    }






}
