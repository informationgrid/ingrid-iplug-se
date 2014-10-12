package de.ingrid.iplug.se.iplug;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.elasticsearch.client.Client;
import org.elasticsearch.cluster.ClusterState;
import org.elasticsearch.cluster.metadata.IndexMetaData;
import org.elasticsearch.cluster.metadata.MappingMetaData;
import org.elasticsearch.common.collect.ImmutableOpenMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.ingrid.iplug.se.SEIPlug;
import de.ingrid.iplug.se.elasticsearch.bean.ElasticsearchNodeFactoryBean;
import de.ingrid.utils.IPlugDescriptionFilter;
import de.ingrid.utils.PlugDescription;

@Service
public class IPlugSEPlugdescriptionFilter implements IPlugDescriptionFilter {

    private static final Log LOG = LogFactory.getLog(IPlugSEPlugdescriptionFilter.class);

    @Autowired
    private ElasticsearchNodeFactoryBean elasticSearch;

    @Override
    public void filter(PlugDescription pd) {

        try {
            Client client = elasticSearch.getObject().client();
            ClusterState clusterState = client.admin().cluster().prepareState().execute().actionGet().getState();
            IndexMetaData inMetaData = clusterState.getMetaData().index(SEIPlug.conf.index);
            ImmutableOpenMap<String, MappingMetaData> metad = inMetaData.getMappings();
            @SuppressWarnings("unchecked")
            List<String> fields = pd.getArrayList(PlugDescription.FIELDS);

            for (Iterator<MappingMetaData> i = metad.valuesIt(); i.hasNext();) {
                MappingMetaData mmd = i.next();
                Map<String, Object> src = (Map<String, Object>) mmd.getSourceAsMap().get("properties");
                for (String f : src.keySet()) {
                    if (!fields.contains(f)) {
                        pd.addField(f);
                    }
                }
            }
            if (!fields.contains("site")) {
                pd.addField("site");
            }

            // make sure only partner=all is communicated to iBus
            @SuppressWarnings("unchecked")
            List<String> partners = pd.getArrayList(PlugDescription.PARTNER);
            //
            if (partners == null) {
                pd.addPartner("all");
            } else {
                for (String partner : partners) {
                    if (!partner.equalsIgnoreCase("all")) {
                        pd.removeFromList(PlugDescription.PARTNER, partner);
                    }
                }
                if (partners.isEmpty()) {
                    partners.add("all");
                }
            }

            // make sure only provider=all is communicated to iBus
            @SuppressWarnings("unchecked")
            List<String> providers = pd.getArrayList(PlugDescription.PROVIDER);
            if (providers == null) {
                pd.addProvider("all");
            } else {
                for (String provider : providers) {
                    if (!provider.equalsIgnoreCase("all")) {
                        pd.removeFromList(PlugDescription.PROVIDER, provider);
                    }
                }
                if (providers.isEmpty()) {
                    providers.add("all");
                }
            }

        } catch (Exception e) {
            LOG.error("Error modifying plugdescription for SE iPlug", e);
        }

    }

}
