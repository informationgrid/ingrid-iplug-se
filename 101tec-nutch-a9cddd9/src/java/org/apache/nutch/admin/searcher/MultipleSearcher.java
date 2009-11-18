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
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.apache.nutch.searcher.Hit;
import org.apache.nutch.searcher.HitDetails;
import org.apache.nutch.searcher.HitSummarizer;
import org.apache.nutch.searcher.Hits;
import org.apache.nutch.searcher.Query;
import org.apache.nutch.searcher.SearchBean;
import org.apache.nutch.searcher.SegmentBean;
import org.apache.nutch.searcher.Summary;

public class MultipleSearcher implements SearchBean, HitSummarizer {

  private final ThreadPool _threadPool;
  private final SearchBean[] _searchBeans;
  private Map<String, SegmentBean> _segmentBeans = new HashMap<String, SegmentBean>();

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
    PriorityQueue<Hit> hitQueue = new PriorityQueue<Hit>(numHits,
            new Comparator<Hit>() {
              @Override
              public int compare(Hit o1, Hit o2) {
                return o1.compareTo(o2);
              }
            });

    // merge hits

    long totalHits = 0;
    for (int i = 0; i < _searchBeans.length; i++) {
      SearchBucket bucket = null;
      Hits hits = null;
      try {
        bucket = arrayBlockingQueue.take();
        hits = bucket.getHits();
      } catch (InterruptedException e) {
        e.printStackTrace();
      }

      if (hits == null) {
        continue;
      }

      totalHits += hits.getTotal();
      int hitsLength = hits.getLength();
      for (int j = 0; j < hitsLength; j++) {
        Hit hit = hits.getHit(j);
        Hit newHit = new Hit(bucket.getId(), hit.getUniqueKey(), hit
                .getSortValue(), hit.getDedupValue());
        hitQueue.add(newHit);
        if (hitQueue.size() > numHits) { // if hit queue overfull
          hitQueue.remove();
        }
      }
    }
    Hit[] culledResults = hitQueue.toArray(new Hit[hitQueue.size()]);
    Arrays.sort(culledResults, Collections.reverseOrder(hitQueue.comparator()));
    return new Hits(totalHits, culledResults);
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
    Summary summary = segmentBean.getSummary(details, query);
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

}
