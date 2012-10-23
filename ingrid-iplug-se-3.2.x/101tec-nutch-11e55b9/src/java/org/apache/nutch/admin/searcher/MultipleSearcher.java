/**
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
package org.apache.nutch.admin.searcher;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.apache.nutch.searcher.Hit;
import org.apache.nutch.searcher.HitDetails;
import org.apache.nutch.searcher.HitSummarizer;
import org.apache.nutch.searcher.Hits;
import org.apache.nutch.searcher.LuceneSearchBean;
import org.apache.nutch.searcher.NutchBean;
import org.apache.nutch.searcher.Query;
import org.apache.nutch.searcher.SearchBean;
import org.apache.nutch.searcher.SegmentBean;
import org.apache.nutch.searcher.Summary;

public class MultipleSearcher implements SearchBean, HitSummarizer {

  final ThreadPool _threadPool;
  final SearchBean[] _searchBeans;

  Map<String, SegmentBean> _segmentBeans = new HashMap<String, SegmentBean>();

  public MultipleSearcher(ThreadPool threadPool,
          SearchBean[] searchBeans, SegmentBean[] segmentBeans)
          throws IOException {
    _threadPool = threadPool;
    _searchBeans = searchBeans;
    for (SegmentBean segmentBean : segmentBeans) {
      String[] segmentNames = segmentBean.getSegmentNames();
      for (String segmentName : segmentNames) {
        _segmentBeans.put(segmentName, segmentBean);
      }
    }
  }

  public void close() throws IOException {
    for (SearchBean searchBean : _searchBeans) {
      searchBean.close();
    }
    _threadPool.close();
  }

  public int getNutchBeanLength() {
    return _searchBeans.length;
  }

  @Override
  public boolean ping() throws IOException {
    return true;
  }

  @Override
  public String getExplanation(Query query, Hit hit) throws IOException {
    return _searchBeans[hit.getIndexNo()].getExplanation(query, hit);
  }

  @Override
  public Hits search(Query query, int numHits, String dedupField,
          String sortField, boolean reverse) throws IOException {
      if (LOG.isDebugEnabled()) {
          LOG.debug("Use " + _searchBeans.length + "search beans.");
      }
    if (_searchBeans.length == 0) {
      return null;
    }
    BlockingQueue<SearchBucket> arrayBlockingQueue = new ArrayBlockingQueue<SearchBucket>(
            _searchBeans.length);

    // start searching in thread
    for (int i = 0; i < _searchBeans.length; i++) {
      SearchRunnable searchRunnable = new SearchRunnable(i, _searchBeans[i],
              query, numHits, dedupField, sortField, reverse,
              arrayBlockingQueue);
      _threadPool.execute(searchRunnable);
    }

    // create queue to merge all hits
    List<Hit> hitQueue = new ArrayList<Hit>();;
    long totalHits = 0;
    boolean hasSeveralSearchBeans = _searchBeans.length > 1 ? true : false;

    // merge hits
    for (int i = 0; i < _searchBeans.length; i++) {
      SearchBucket bucket = null;
      Hits hits = null;
      try {
        bucket = arrayBlockingQueue.take();
        hits = bucket.getHits();
        if (LOG.isDebugEnabled()) {
            try {
                LOG.debug("Found " + hits.getLength() + "hits in index '" + ((LuceneSearchBean)((NutchBean)_searchBeans[bucket.getId()]).getSearchBean()).getSearcher().getReader().directory().toString() + "'.");
            } catch (Exception e) {
                LOG.debug("Error creating debug message for multiple searchers.", e);
            }
        }
      } catch (InterruptedException e) {
        e.printStackTrace();
      }

      if (hits == null) {
        continue;
      }

      totalHits += hits.getTotal();
      int hitsLength = hits.getLength();
      for (int j = 0; j < hitsLength; j++) {
        if (hitQueue.size() == numHits) {
          break;
        }
        Hit hit = hits.getHit(j);
        Hit newHit = new Hit(bucket.getId(), hit.getUniqueKey(), hit
                .getSortValue(), hit.getDedupValue());
        // if we have more than one SearchBean, then we have to merge the results
        // with the other one, otherwise just add it to the list
        if (hasSeveralSearchBeans)
            mergeElement(hitQueue, newHit);
        else 
            hitQueue.add(newHit);
      }
    }
    
    Hit[] culledResults = hitQueue.toArray(new Hit[hitQueue.size()]);
    return new Hits(totalHits, culledResults);
  }
  
  /**
   * Merge a hit to a list according to its score. It will added behind the last hit
   * with a higher or equal score!
   * @param l is the list to add the the
   * @param hit is the hit to be added
   */
  protected void mergeElement(List<Hit> l, Hit hit) {
      // start from the end (lowest score)
      for (int i=l.size()-1; i>=0; i--) {
          Hit hitFromList = l.get(i);
          if (hit.getSortValue().compareTo(hitFromList.getSortValue()) <= 0) {
              l.add(i+1,hit);
              return;
          }
      }
      // add to the front
      l.add(0,hit);
  }

  @Override
  public HitDetails getDetails(Hit hit) throws IOException {
    return _searchBeans[hit.getIndexNo()].getDetails(hit);
  }

  @Override
  public HitDetails[] getDetails(Hit[] hits) throws IOException {
    HitDetails[] details = new HitDetails[hits.length];
    for (int i = 0; i < hits.length; i++) {
      details[i] = getDetails(hits[i]);
    }
    return details;
  }

  @Override
  public Summary getSummary(HitDetails details, Query query) throws IOException {
    String segmentName = details.getValue("segment");
    SegmentBean segmentBean = _segmentBeans.get(segmentName);
    Summary summary;
    if (segmentBean == null) {
        LOG.error("CrawlDB is corrupt. Referenz to segment '" + segmentName + "' is invalid");
        summary = new Summary();
    } else {
        summary = segmentBean.getSummary(details, query);
    }
    return summary;
  }

  @Override
  public Summary[] getSummary(HitDetails[] details, Query query)
          throws IOException {
    Summary[] summaries = new Summary[details.length];
    for (int i = 0; i < details.length; i++) {
      summaries[i] = getSummary(details[i], query);
    }
    return summaries;
  }
  
  public SearchBean[] getSearchBeans() {
      return _searchBeans;
  }


}
