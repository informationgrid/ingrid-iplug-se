package de.ingrid.iplug.se.iplug;

/**
 * Processors implementing this interface will be executed after the successful crawl process. 
 * 
 * 
 * @author joachim
 *
 */
public interface IPostCrawlProcessor {
    
    
    /**
     * Executed after the crawl process.
     * 
     */
    public void execute();

}
