package de.ingrid.iplug.se.nutchController;

import de.ingrid.iplug.se.webapp.container.Instance;

/**
 * Represents a crawl.
 * 
 * @author joachim
 *
 */
public class Crawl {
    
    Integer depth = null;
    
    Integer noUrls = null;
    
    Instance instance = null;

    public Integer getDepth() {
        return depth;
    }

    public void setDepth(Integer depth) {
        this.depth = depth;
    }

    public Integer getNoUrls() {
        return noUrls;
    }

    public void setNoUrls(Integer noUrls) {
        this.noUrls = noUrls;
    }

    public Instance getInstance() {
        return instance;
    }

    public void setInstance(Instance instance) {
        this.instance = instance;
    }
    
    
    
}
