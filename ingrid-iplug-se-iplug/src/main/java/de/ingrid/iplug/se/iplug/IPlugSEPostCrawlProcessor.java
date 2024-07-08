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
package de.ingrid.iplug.se.iplug;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.mapping.Property;
import co.elastic.clients.elasticsearch.indices.get_mapping.IndexMappingRecord;
import de.ingrid.admin.Config;
import de.ingrid.admin.service.PlugDescriptionService;
import de.ingrid.elasticsearch.ElasticsearchNodeFactoryBean;
import de.ingrid.iplug.se.webapp.container.Instance;
import de.ingrid.utils.PlugDescription;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Adds all fields in the index to the plugdescription AND configuration.
 *
 * @author joachim
 *
 */
@Service
public class IPlugSEPostCrawlProcessor implements IPostCrawlProcessor {

    private static final Log LOG = LogFactory.getLog(IPlugSEPostCrawlProcessor.class);

    @Autowired
    private ElasticsearchNodeFactoryBean elasticSearch;

    @Autowired
    private PlugDescriptionService plugDescriptionService;

    @Autowired
    private Config baseConfig;

    @Override
    public void execute(Instance instance) {

        try {
            PlugDescription pd = plugDescriptionService.getPlugDescription();

            List<Object> fields = pd.getArrayList(PlugDescription.FIELDS);
            if (fields == null) {
                fields = new ArrayList<>();
                pd.put(PlugDescription.FIELDS, fields);
            }

            ElasticsearchClient client = elasticSearch.getClient();
            if (client != null) {
                Map<String, IndexMappingRecord> mappings = client.indices().getMapping(m -> m.index(instance.getInstanceIndexName())).result();
                for(IndexMappingRecord mmd : mappings.values()) {
                    @SuppressWarnings("unchecked")
                    Map<String, Property> src = mmd.mappings().properties();
                    for (String f : src.keySet()) {
                        if (!fields.contains(f)) {
                            pd.addField(f);
                        }
                    }
                }
                if (!fields.contains("site")) {
                    pd.addField("site");
                }
            } else {
                LOG.warn("Unable to add fields from Elasticsearch index mapping to plugdescription. No Elasticsearch client found.");
            }

            plugDescriptionService.savePlugDescription(pd);
            baseConfig.writePlugdescriptionToProperties(plugDescriptionService.getCommandObect());

        } catch (Exception e) {
            LOG.error("Error adding index fields to plugdescription.", e);
        }

    }

}
