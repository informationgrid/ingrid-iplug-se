/*
 * **************************************************-
 * ingrid-iplug-se-iplug
 * ==================================================
 * Copyright (C) 2014 - 2018 wemove digital solutions GmbH
 * ==================================================
 * Licensed under the EUPL, Version 1.1 or – as soon they will be
 * approved by the European Commission - subsequent versions of the
 * EUPL (the "Licence");
 * 
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl5
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * **************************************************#
 */
package de.ingrid.iplug.se.iplug;

import java.io.IOException;
import java.util.*;

import de.ingrid.elasticsearch.ElasticsearchNodeFactoryBean;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.IndexNotFoundException;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms.Bucket;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.ingrid.admin.JettyStarter;
import de.ingrid.utils.PlugDescription;
import de.ingrid.utils.metadata.IPlugOperatorFinder;

@Service
public class IPlugSeOperatorFinder implements IPlugOperatorFinder {

    private static final Log LOG = LogFactory.getLog(IPlugSeOperatorFinder.class);
    
    boolean missingIndexExceptionAlreadyThrown = false;

    @Autowired
    private ElasticsearchNodeFactoryBean elasticSearch;

    public Set<String> findIndexValues(String indexFieldName) throws Exception {

        Client client = elasticSearch.getClient();
        SearchResponse response = client.prepareSearch(JettyStarter.getInstance().config.index).setQuery(QueryBuilders.matchAllQuery()).addAggregation(AggregationBuilders.terms("TermsAggr").field(indexFieldName).size(0)).execute().actionGet();

        Terms terms = response.getAggregations().get("TermsAggr");
        List<? extends Bucket> buckets = terms.getBuckets();
        Set<String> valueSet = new HashSet<>();
        for (Bucket b : buckets) {
            valueSet.add(b.getKeyAsString());
        }
        this.missingIndexExceptionAlreadyThrown = false;
        return valueSet;
    }

    @Override
    public Set<String> findPartner() throws IOException {
        try {
            return findIndexValues("partner");
        } catch (IndexNotFoundException e) {
            if (!this.missingIndexExceptionAlreadyThrown) {
                this.missingIndexExceptionAlreadyThrown = true;
                LOG.warn( "Index does not exist." );
            }
            return new HashSet<>();
        } catch (Exception e) {
            LOG.error("Error obtaining partners from index.", e);
            throw new IOException(e);
        }
    }

    @Override
    public Set<String> findProvider() throws IOException {
        try {
            return findIndexValues("provider");
        } catch (IndexNotFoundException e) {
            if (!this.missingIndexExceptionAlreadyThrown) {
                this.missingIndexExceptionAlreadyThrown = true;
                LOG.warn( "Index does not exist." );
            }
            return new HashSet<>();
        } catch (Exception e) {
            LOG.error("Error obtaining providers from index.", e);
            throw new IOException(e);
        }
    }

    @Override
    public void configure(PlugDescription plugDescription) {
    }

}
