/*
 * **************************************************-
 * ingrid-iplug-se-iplug
 * ==================================================
 * Copyright (C) 2014 - 2025 wemove digital solutions GmbH
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
package de.ingrid.iplug.se.iplug;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import co.elastic.clients.elasticsearch._types.aggregations.Aggregate;
import co.elastic.clients.elasticsearch._types.aggregations.AggregationBuilders;
import co.elastic.clients.elasticsearch._types.aggregations.Buckets;
import co.elastic.clients.elasticsearch._types.aggregations.MultiTermsBucket;
import co.elastic.clients.elasticsearch._types.query_dsl.MatchAllQuery;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import de.ingrid.admin.Config;
import de.ingrid.elasticsearch.ElasticsearchNodeFactoryBean;
import de.ingrid.utils.ElasticDocument;
import de.ingrid.utils.PlugDescription;
import de.ingrid.utils.metadata.IPlugOperatorFinder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class IPlugSeOperatorFinder implements IPlugOperatorFinder {

    private static final Log LOG = LogFactory.getLog(IPlugSeOperatorFinder.class);

    boolean missingIndexExceptionAlreadyThrown = false;

    @Autowired
    private ElasticsearchNodeFactoryBean elasticSearch;

    @Autowired
    private Config baseConfig;

    public Set<String> findIndexValues(String indexFieldName) throws Exception {

        ElasticsearchClient client = elasticSearch.getClient();
        SearchResponse<ElasticDocument> response = client.search(s -> s
                .index(baseConfig.index)
                .query(new MatchAllQuery.Builder().build()._toQuery())
                .aggregations("TermsAggr", AggregationBuilders.terms(agg -> agg.field(indexFieldName).size(0))),
        ElasticDocument.class);

        Aggregate terms = response.aggregations().get("TermsAggr");
        Buckets<MultiTermsBucket> buckets = terms.multiTerms().buckets();
        Set<String> valueSet = new HashSet<>();
        for (MultiTermsBucket b : buckets.array()) {
            valueSet.add(b.keyAsString());
        }
        this.missingIndexExceptionAlreadyThrown = false;
        return valueSet;
    }

    @Override
    public Set<String> findPartner() throws IOException {
        try {
            return findIndexValues("partner");
        } catch (ElasticsearchException e) {
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
        } catch (ElasticsearchException e) {
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
