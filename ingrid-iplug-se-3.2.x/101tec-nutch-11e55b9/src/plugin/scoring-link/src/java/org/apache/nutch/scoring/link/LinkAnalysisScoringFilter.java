/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.nutch.scoring.link;

import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.Text;
import org.apache.nutch.crawl.CrawlDatum;
import org.apache.nutch.crawl.Inlinks;
import org.apache.nutch.indexer.NutchDocument;
import org.apache.nutch.metadata.Nutch;
import org.apache.nutch.parse.Parse;
import org.apache.nutch.parse.ParseData;
import org.apache.nutch.protocol.Content;
import org.apache.nutch.scoring.ScoringFilter;
import org.apache.nutch.scoring.ScoringFilterException;
import org.apache.nutch.util.LogUtil;

public class LinkAnalysisScoringFilter
  implements ScoringFilter {

    private final static Log LOG = LogFactory.getLog(LinkAnalysisScoringFilter.class);
    
    
  private Configuration conf;
  private float scoreInjected = 0.001f;
  private float normalizedScore = 1.00f;
  
  private float sortScoreDiffdaysWeight = 0.0f;

  public LinkAnalysisScoringFilter() {

  }

  public Configuration getConf() {
    return conf;
  }

  public void setConf(Configuration conf) {
    this.conf = conf;
    normalizedScore = conf.getFloat("link.analyze.normalize.score", 1.00f);
    scoreInjected = conf.getFloat("link.analyze.injected.score", 1.00f);
    sortScoreDiffdaysWeight = conf.getFloat("link.analyze.generate.sort.diffdays.weight", 0.00f);
  }

  public CrawlDatum distributeScoreToOutlinks(Text fromUrl,
    ParseData parseData, Collection<Entry<Text, CrawlDatum>> targets,
    CrawlDatum adjust, int allCount)
    throws ScoringFilterException {
      
      // set the score of all outlinks to the score of the parent url 
      // devided by the number of outlinks
      // 25.03.2011 joachim@wemove.com
      float score = scoreInjected;
      String scoreString = parseData.getContentMeta().get(Nutch.SCORE_KEY);
      if (scoreString != null) {
        try {
          score = Float.parseFloat(scoreString);
        } catch (Exception e) {
          e.printStackTrace(LogUtil.getWarnStream(LOG));
        }
      }
      int validCount = targets.size();
      if (validCount == 0) {
        // no outlinks to distribute score, so just return adjust
        return adjust;
      }
      score /= validCount;
      for (Entry<Text, CrawlDatum> target : targets) {
           target.getValue().setScore(score);
      }

      return adjust;
  }

  public float generatorSortValue(Text url, CrawlDatum datum, float initSort)
    throws ScoringFilterException {
    // increase the sort value
    long diff =  System.currentTimeMillis() - datum.getFetchTime();
    // difference between current time and fetch time in days
    // if the datum is due to fetch, increase the score by this
    // value multiplied with a weight to push long due entries
    // 2011-10-05 joachim@wemove.com
    float diffDays = 0;
    if (diff > 0) {
      diffDays = diff * 1.0f / 24.0f / 3600.0f / 1000.0f; 
    }
    return datum.getScore() * initSort + diffDays*sortScoreDiffdaysWeight;
  }

  public float indexerScore(Text url, NutchDocument doc, CrawlDatum dbDatum,
    CrawlDatum fetchDatum, Parse parse, Inlinks inlinks, float initScore)
    throws ScoringFilterException {
    return (normalizedScore * dbDatum.getScore())+0.001f;
  }

  public void initialScore(Text url, CrawlDatum datum)
    throws ScoringFilterException {
    datum.setScore(0.0f);
  }

  public void injectedScore(Text url, CrawlDatum datum)
    throws ScoringFilterException {
    datum.setScore(scoreInjected);
  }

  public void passScoreAfterParsing(Text url, Content content, Parse parse)
    throws ScoringFilterException {
    parse.getData().getContentMeta().set(Nutch.SCORE_KEY,
      content.getMetadata().get(Nutch.SCORE_KEY));
  }

  public void passScoreBeforeParsing(Text url, CrawlDatum datum, Content content)
    throws ScoringFilterException {
    content.getMetadata().set(Nutch.SCORE_KEY, "" + datum.getScore());
  }

  public void updateDbScore(Text url, CrawlDatum old, CrawlDatum datum,
    List<CrawlDatum> inlinked)
    throws ScoringFilterException {
      // set the score of unfetched urls to the score of the first inlinked crawldatum
      // the inlinked crawldatum represents the score, derived from the parent url
      // see method distributeScoreToOutlinks()
      // 25.03.2011 joachim@wemove.com
      if (datum.getStatus() == CrawlDatum.STATUS_DB_UNFETCHED) {
          if (inlinked != null && inlinked.size() > 0) {
              datum.setScore(inlinked.get(0).getScore());
          }
      }
  }

}
