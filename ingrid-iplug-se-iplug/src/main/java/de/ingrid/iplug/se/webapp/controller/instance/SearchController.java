/*
 * **************************************************-
 * ingrid-iplug-se-iplug
 * ==================================================
 * Copyright (C) 2014 - 2024 wemove digital solutions GmbH
 * ==================================================
 * Licensed under the EUPL, Version 1.2 or – as soon they will be
 * approved by the European Commission - subsequent versions of the
 * EUPL (the "Licence");
 * 
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * **************************************************#
 */
package de.ingrid.iplug.se.webapp.controller.instance;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import de.ingrid.elasticsearch.ElasticConfig;
import de.ingrid.elasticsearch.IndexInfo;
import de.ingrid.iplug.se.SEIPlug;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;

import de.ingrid.iplug.HeartBeatPlug;
import de.ingrid.iplug.se.webapp.container.Instance;
import de.ingrid.iplug.se.webapp.controller.AdminViews;
import de.ingrid.utils.IRecordLoader;
import de.ingrid.utils.IngridHit;
import de.ingrid.utils.IngridHitDetail;
import de.ingrid.utils.IngridHits;
import de.ingrid.utils.query.IngridQuery;
import de.ingrid.utils.queryparser.QueryStringParser;
import de.ingrid.utils.queryparser.TokenMgrError;

/**
 * Control the database parameter page.
 * 
 * @author joachim@wemove.com
 * 
 */
@Controller
@SessionAttributes("plugDescription")
public class SearchController extends InstanceController {

    private final HeartBeatPlug _plug;

    @Autowired
    private ElasticConfig elasticConfig;

    @Autowired
    public SearchController(final HeartBeatPlug plug) throws Exception {
        _plug = plug;
    }

    @RequestMapping(value = { "/iplug-pages/instanceSearch.html" }, method = RequestMethod.GET)
    public String showSearch(final ModelMap modelMap, @RequestParam(value = "instance", required = false) String name, HttpServletRequest request, HttpServletResponse response) {

        if (hasNoAccessToInstance(name, request, response)) {
            return redirect( AdminViews.SE_LIST_INSTANCES + ".html" );
        }

        Instance instance = null;
        // if no instance name was found or no belonging directory then show the instance list page
        if (name == null || (instance = InstanceController.getInstanceData( name )) == null) {
            return redirect( AdminViews.SE_LIST_INSTANCES + ".html" );
        } else {
            modelMap.put( "instance", instance );
            return AdminViews.SE_INSTANCE_SEARCH;
        }
    }

    @RequestMapping(value = { "/iplug-pages/instanceSearch.html" }, method = RequestMethod.POST)
    public String doQuery(final ModelMap modelMap,
            @RequestParam(value = "query", required = false) final String queryString,
            @RequestParam("instance") String instance, HttpServletRequest request, HttpServletResponse response) throws Exception {

        if (hasNoAccessToInstance(instance, request, response)) {
            return redirect( AdminViews.SE_LIST_INSTANCES + ".html" );
        }

        modelMap.addAttribute( "instance", InstanceController.getInstanceData( instance ) );
        
        if (queryString != null) {
            modelMap.addAttribute( "query", queryString );
            IngridQuery query = null;
            try {
                query = QueryStringParser.parse( queryString );
            } catch (TokenMgrError e) {
                return AdminViews.SE_INSTANCE_SEARCH;
            }

            // add instance information into query which is understood by this
            // iPlug
            IndexInfo indexInfo = new IndexInfo();
            indexInfo.setToAlias(SEIPlug.baseConfig.index + "_" + instance);
            indexInfo.setToIndex(SEIPlug.baseConfig.index + "_" + instance);
            indexInfo.setToType("default");
            elasticConfig.activeIndices = new IndexInfo[] {indexInfo};

            final IngridHits results = _plug.search( query, 0, 20 );
            modelMap.addAttribute( "totalHitCount", results.length() );

            final IngridHit[] hits = results.getHits();
            final IngridHitDetail[] details = _plug.getDetails( hits, query, new String[] {"title", "summary", "url"} );

            // convert details to map
            // this is necessary because it's not possible to access the
            // document-id by ${hit.documentId}
            final Map<String, IngridHitDetail> detailsMap = new HashMap<String, IngridHitDetail>();
            if (details != null) {
                for (final IngridHitDetail detail : details) {
                    if (detail.get("url") != null && detail.get("url") instanceof String[] && ((String[]) detail.get("url")).length > 0) {
                        detail.put("url", ((String[]) detail.get("url"))[0]);
                    }
                    detailsMap.put( detail.getDocumentId(), detail );
                }
            }

            modelMap.addAttribute( "hitCount", details.length );
            modelMap.addAttribute( "hits", detailsMap );
            modelMap.addAttribute( "details", _plug instanceof IRecordLoader );
        }

        return AdminViews.SE_INSTANCE_SEARCH;
    }

}
