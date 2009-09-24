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

import java.util.concurrent.BlockingQueue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.nutch.searcher.Hits;
import org.apache.nutch.searcher.Query;
import org.apache.nutch.searcher.SearchBean;

public class SearchRunnable implements Runnable {

  private final BlockingQueue<SearchBucket> _queue;
  private final String _dedupField;
  private final String _sortField;
  private final boolean _reverse;
  private SearchBucket _empty = new SearchBucket();
  private final int _id;
  private final SearchBean _searchBean;
  private final int _numHits;
  private final Query _query;
  private static final Log LOG = LogFactory.getLog(SearchRunnable.class);

  public SearchRunnable(int id, SearchBean searchBean, Query query,
          int numHits, String dedupField, String sortField, boolean reverse,
          BlockingQueue<SearchBucket> queue) {
    _id = id;
    _searchBean = searchBean;
    _query = query;
    _numHits = numHits;
    _dedupField = dedupField;
    _sortField = sortField;
    _reverse = reverse;
    _queue = queue;
  }

  @Override
  public void run() {
    try {
      if (LOG.isDebugEnabled()) {
        LOG.debug("start to search with bean [" + _searchBean
                + "] with query [" + _query + "]");
      }
      Hits hits = _searchBean.search(_query, _numHits, _dedupField, _sortField,
              _reverse);
      if (LOG.isDebugEnabled()) {
        LOG.debug("bean [" + _searchBean + "] find hits [" + hits.getLength()
                + "]");
      }
      SearchBucket bucket = new SearchBucket(_id, hits);
      _queue.put(bucket);
    } catch (Exception e) {
      LOG.warn("error while searching", e);
      try {
        _queue.put(_empty);
      } catch (InterruptedException e1) {
        LOG.warn("error while putting empty search bucket into queue");
      }
    }
  }

}
