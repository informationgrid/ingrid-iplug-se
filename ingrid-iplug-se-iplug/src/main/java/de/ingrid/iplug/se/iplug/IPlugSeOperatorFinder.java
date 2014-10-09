package de.ingrid.iplug.se.iplug;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms.Bucket;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.ingrid.iplug.se.SEIPlug;
import de.ingrid.iplug.se.elasticsearch.bean.ElasticsearchNodeFactoryBean;
import de.ingrid.utils.PlugDescription;
import de.ingrid.utils.metadata.IPlugOperatorFinder;

@Service
public class IPlugSeOperatorFinder implements IPlugOperatorFinder {

    private static final Log LOG = LogFactory.getLog(IPlugSeOperatorFinder.class);

    @Autowired
    private ElasticsearchNodeFactoryBean elasticSearch;

    public Set<String> findIndexValues(String indexFieldName) throws Exception {

        Client client = elasticSearch.getObject().client();
        SearchResponse response = client.prepareSearch(SEIPlug.conf.index).setQuery(QueryBuilders.matchAllQuery()).addAggregation(AggregationBuilders.terms("TermsAggr").field(indexFieldName).size(0)).execute().actionGet();

        Terms terms = response.getAggregations().get("TermsAggr");
        Collection<Bucket> buckets = terms.getBuckets();
        Set<String> valueSet = new HashSet<String>();
        for (Bucket b : buckets) {
            valueSet.add(b.getKey());
        }
        return valueSet;
    }

    @Override
    public Set<String> findPartner() throws IOException {
        try {
            return findIndexValues("partner");
        } catch (Exception e) {
            LOG.error("Error obzaining partners from index.", e);
            throw new IOException(e);
        }
    }

    @Override
    public Set<String> findProvider() throws IOException {
        try {
            return findIndexValues("provider");
        } catch (Exception e) {
            LOG.error("Error obzaining partners from index.", e);
            throw new IOException(e);
        }
    }

    @Override
    public void configure(PlugDescription plugDescription) {
    }

}
