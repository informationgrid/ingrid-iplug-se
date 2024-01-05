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
package de.ingrid.iplug.se;

import de.ingrid.admin.IConfig;
import de.ingrid.admin.IKeys;
import de.ingrid.admin.command.PlugdescriptionCommandObject;
import de.ingrid.utils.PlugDescription;
import de.ingrid.utils.query.IngridQuery;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

@org.springframework.context.annotation.Configuration
@PropertySource(
        value = {"classpath:elasticsearch.properties", "classpath:elasticsearch.override.properties"},
        ignoreResourceNotFound = true)
public class Configuration implements IConfig {

    private static Logger log = Logger.getLogger(Configuration.class);

    @Override
    public void initialize() {
        // disable the default index menu of the base webapp
        // we still have to use the option "indexing=true" to enable elastic search
        System.clearProperty(IKeys.INDEXING);

        // the results from this iPlug have to be grouped by its' domain and not by the iPlug ID
        // see at IndexImpl.java in base-webapp for implementation
        // TODO: baseConfig.groupByUrl = true;
    }

    @Value("${dir.instances:instances}")
    private String dirInstances;

    @Value("${db.id:iplug-se}")
    public String databaseID;

    @Value("${db.dir:database}")
    public String databaseDir;

    @Value("${transport.tcp.port:9300}")
    public String esTransportTcpPort;

    @Value("${network.host:localhost}")
    public String esHttpHost;

    @Value("${cluster.name:ingrid}")
    public String clusterName;

    @Value("#{'${nutch.call.java.options:-Dhadoop.log.file=hadoop.log -Dfile.encoding=UTF-8}'.split(' ')}")
    public List<String> nutchCallJavaOptions;

    @Value("${nutch.call.java.executable:java}")
    public String nutchCallJavaExecutable;

    @Value("${plugdescription.fields:}")
    public List<String> fields;

    @Value("${dependingFields:}")
    public List<String> dependingFields;

    public Map<String, String> facetMap;

    public Map<String, String> queryFieldMap;


    @Override
    public void addPlugdescriptionValues(PlugdescriptionCommandObject pdObject) {
        log.info("Add iPlug specific properties into plugdescription.");

        pdObject.put("iPlugClass", "de.ingrid.iplug.se.SEIPlug");

        // make sure only partner=all is communicated to iBus
        List<Object> partners = pdObject.getArrayList(PlugDescription.PARTNER);
        //
        if (partners == null) {
            pdObject.addPartner("all");
        } else {
            partners.clear();
            partners.add("all");
        }

        // make sure only provider=all is communicated to iBus
        List<Object> providers = pdObject.getArrayList(PlugDescription.PROVIDER);
        if (providers == null) {
            pdObject.addProvider("all");
        } else {
            providers.clear();
            providers.add("all");
        }

        if (!pdObject.containsRankingType(IngridQuery.SCORE_RANKED)) {
            pdObject.addToList(IngridQuery.RANKED, IngridQuery.SCORE_RANKED);
        }

        // add fields
        List<Object> pdFields = pdObject.getArrayList(PlugDescription.FIELDS);
        for (String field : fields) {
            if (field != null && !field.isEmpty() && pdFields != null && !pdFields.contains(field)) {
                pdObject.addField(field);
            }
        }
    }

    @Override
    public void setPropertiesFromPlugdescription(Properties props, PlugdescriptionCommandObject pd) {
        props.setProperty("db.dir", this.databaseDir);
        props.setProperty("dir.instances", this.dirInstances);
    }

    public String getInstancesDir() {
        return dirInstances;
    }

    public void setInstancesDir(String dir) {
        this.dirInstances = dir;
    }

    @Value("${facetMapping:air->measure:air,radiation->measure:radiation,water->measure:water,misc->measure:misc,press->service:press,publication->service:publication,event->service:event}")
    private void setFacetMap(String input) {
        this.facetMap = getMapFromInput(input);
    }

    @Value("${queryFieldMapping:topic:air->measure:air,topic:radiation->measure:radiation,topic:water->measure:water,topic:misc->measure:misc,topic:press->service:press,topic:publication->service:publication,topic:event->service:event}")
    private void setQueryFieldMap(String input) {
        this.queryFieldMap = getMapFromInput(input);
    }

    private Map<String, String> getMapFromInput(String input) {
        Map<String, String> map = new HashMap<>();
        if (!"".equals(input)) {
            String[] entries = input.split(",");
            for (String entry : entries) {
                String[] split = entry.split("->");
                map.put(split[0], split[1]);
            }
        }
        return map;
    }

}
