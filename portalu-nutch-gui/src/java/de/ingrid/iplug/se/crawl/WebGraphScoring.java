package de.ingrid.iplug.se.crawl;

import java.io.IOException;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.nutch.scoring.webgraph.LinkRank;
import org.apache.nutch.scoring.webgraph.ScoreUpdater;
import org.apache.nutch.scoring.webgraph.WebGraph;
import org.apache.nutch.util.NutchJob;

public class WebGraphScoring {
    
    public static final Log LOG = LogFactory.getLog(WebGraphScoring.class);
    
    String webGraphDbString;
    WebGraph webGraph;
    LinkRank linkRank;
    ScoreUpdater scoreUpdater;
    
    public WebGraphScoring(Configuration conf) {
        webGraphDbString = conf.get("nutch.instance.folder") + "/webGraphDB";
        webGraph = new WebGraph(conf);
        linkRank = new LinkRank(conf);
        scoreUpdater = new ScoreUpdater(conf);
        scoreUpdater.configure(new NutchJob(conf));
    }
    
    public void updateScore(Path crawlDb, List<Path> segments) {
        // using webgraph score
        // see: http://markmail.org/message/cov4lyvg4p6zwdm4#query:nutch%20scoring%20method+page:1+mid:cov4lyvg4p6zwdm4+state:results
        try {
            Path webGraphDbPath = new Path(webGraphDbString);
            Path[] segPaths = new Path[segments.size()];
            for (int i = 0; i < segments.size(); i++) {
                segPaths[i] = segments.get(i);
              }
            webGraph.createWebGraph(webGraphDbPath, segPaths);

            linkRank.analyze(webGraphDbPath);
            
            scoreUpdater.update(crawlDb, webGraphDbPath);
        } catch (IOException e) {
            LOG.error("Error while updating the score via WebGraph!");
            e.printStackTrace();
        }
    }
}
