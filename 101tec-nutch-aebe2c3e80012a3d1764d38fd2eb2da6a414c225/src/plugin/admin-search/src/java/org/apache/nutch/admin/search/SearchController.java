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
package org.apache.nutch.admin.search;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;

import org.apache.hadoop.conf.Configuration;
import org.apache.nutch.admin.NavigationSelector;
import org.apache.nutch.admin.NutchInstance;
import org.apache.nutch.admin.searcher.MultipleSearcher;
import org.apache.nutch.admin.searcher.SearcherFactory;
import org.apache.nutch.searcher.Hit;
import org.apache.nutch.searcher.HitDetails;
import org.apache.nutch.searcher.Hits;
import org.apache.nutch.searcher.Query;
import org.apache.nutch.searcher.Summary;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class SearchController extends NavigationSelector {

  @RequestMapping(method = RequestMethod.GET, value = "/index.html")
  public String welcome(Model model) {
    return "search";
  }

  @RequestMapping(method = RequestMethod.GET, value = "/search.html")
  public String search(@RequestParam("query") String queryString,
          @RequestParam("start") Integer start,
          @RequestParam("length") Integer length, Model model,
          HttpSession session) throws IOException {
    ServletContext servletContext = session.getServletContext();
    NutchInstance nutchInstance = (NutchInstance) servletContext
            .getAttribute("nutchInstance");
    Configuration configuration = nutchInstance.getConfiguration();
    SearcherFactory searcherFactory = SearcherFactory
            .getInstance(configuration);
    MultipleSearcher searcher = searcherFactory.get(false);
    Query query = Query.parse(queryString, configuration);
    int numHits = start + length;
    Hits hits = searcher.search(query, numHits, null, null, false);

    long total = 0;
    List<SearchResult> list = new ArrayList<SearchResult>();
    if (hits != null && hits.getLength() > 0) {

      int end = (int) Math.min(hits.getLength(), start + length);
      int realLength = end - start;
      int realEnd = (int) Math.min(hits.getLength(), start + realLength);

      total = hits.getTotal();
      Hit[] show = hits.getHits(start, realEnd - start);
      HitDetails[] details = searcher.getDetails(show);
      for (int i = 0; i < details.length; i++) {
        HitDetails hitDetails = details[i];
        Summary summary = searcher.getSummary(hitDetails, query);
        String url = hitDetails.getValue("url");
        String title = hitDetails.getValue("title");
        String summaryAsString = summary.toString();
        SearchResult searchResult = new SearchResult(url, title,
                summaryAsString);
        list.add(searchResult);
      }
    }
    model.addAttribute("totalHits", total);
    model.addAttribute("searchResults", list);
    model.addAttribute("page", (start / length) + 1);
    model.addAttribute("query", query.toString());
    return "search";
  }

}
