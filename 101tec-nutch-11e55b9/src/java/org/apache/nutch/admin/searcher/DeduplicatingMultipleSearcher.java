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
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.apache.nutch.searcher.Hit;
import org.apache.nutch.searcher.HitDetails;
import org.apache.nutch.searcher.Hits;
import org.apache.nutch.searcher.NutchBean;
import org.apache.nutch.searcher.Query;
import org.apache.nutch.searcher.SearchBean;
import org.apache.nutch.searcher.SegmentBean;

public class DeduplicatingMultipleSearcher extends MultipleSearcher {

    public DeduplicatingMultipleSearcher(ThreadPool threadPool, SearchBean[] searchBeans, SegmentBean[] segmentBeans)
            throws IOException {
        super(threadPool, searchBeans, segmentBeans);
    }

    @Override
    public Hits search(Query query, int numHits, String dedupField, String sortField, boolean reverse)
            throws IOException {
        if (_searchBeans.length == 0) {
            return null;
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("Use " + _searchBeans.length + "search beans.");
        }
        
        BlockingQueue<SearchBucket> arrayBlockingQueue = new ArrayBlockingQueue<SearchBucket>(_searchBeans.length);

        // start searching in thread
        for (int i = 0; i < _searchBeans.length; i++) {
            SearchRunnable searchRunnable = new SearchRunnable(i, _searchBeans[i], query, numHits, dedupField,
                    sortField, reverse, arrayBlockingQueue);
            _threadPool.execute(searchRunnable);
        }

        // create queue to merge all hits
        List<Hit> hitQueue = new ArrayList<Hit>();

        long totalHits = 0;
        boolean hasSeveralSearchBeans = _searchBeans.length > 1 ? true : false;

        // merge hits
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
                Hit newHit = new Hit(bucket.getId(), hit.getUniqueKey(), hit.getSortValue(), hit.getDedupValue());
                // if we have more than one SearchBean, then we have to merge
                // the results
                // with the other one, otherwise just add it to the list
                if (hasSeveralSearchBeans)
                    mergeElement(hitQueue, newHit);
                else
                    hitQueue.add(newHit);
            }
        }

        // filter duplicates
        if (hasSeveralSearchBeans) {
            int cnt = 0;
            Map<String, String> urlMap = new HashMap<String, String>();
            Iterator<Hit> it = hitQueue.iterator();
            while (it.hasNext()) {
                Hit hit = it.next();
                HitDetails details = _searchBeans[hit.getIndexNo()].getDetails(hit);
                String url = details.getValue("url");
                if (urlMap.containsKey(url)) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Deduplicate: " + url + "(source/segment: " + Arrays.toString(((NutchBean)_searchBeans[hit.getIndexNo()]).getSegmentNames()) + ")");
                    }
                    it.remove();
                    totalHits--;
                } else {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Add: " + url + "(source/segment: " + Arrays.toString(((NutchBean)_searchBeans[hit.getIndexNo()]).getSegmentNames()) + ")");
                    }
                    urlMap.put(url, url);
                    cnt++;
                }
                if (cnt == numHits) {
                    break;
                }
            }
        }

        int realHits = numHits > hitQueue.size() ? hitQueue.size() : numHits;

        Hit[] culledResults = hitQueue.subList(0, realHits).toArray(new Hit[0]);
        return new Hits(totalHits, culledResults);
    }

}
