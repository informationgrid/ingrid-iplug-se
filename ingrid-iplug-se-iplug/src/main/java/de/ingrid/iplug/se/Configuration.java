/*
 * **************************************************-
 * ingrid-iplug-se-iplug
 * ==================================================
 * Copyright (C) 2014 - 2017 wemove digital solutions GmbH
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
package de.ingrid.iplug.se;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import com.tngtech.configbuilder.annotation.configuration.Separator;
import com.tngtech.configbuilder.annotation.propertyloaderconfiguration.PropertiesFiles;
import com.tngtech.configbuilder.annotation.propertyloaderconfiguration.PropertyLocations;
import com.tngtech.configbuilder.annotation.typetransformer.TypeTransformer;
import com.tngtech.configbuilder.annotation.typetransformer.TypeTransformers;
import com.tngtech.configbuilder.annotation.valueextractor.DefaultValue;
import com.tngtech.configbuilder.annotation.valueextractor.PropertyValue;

import de.ingrid.admin.IConfig;
import de.ingrid.admin.IKeys;
import de.ingrid.admin.JettyStarter;
import de.ingrid.admin.command.PlugdescriptionCommandObject;
import de.ingrid.utils.PlugDescription;
import de.ingrid.utils.query.IngridQuery;

@PropertiesFiles( {"config", "elasticsearch"} )
@PropertyLocations(directories = {"conf"}, fromClassLoader = true)
public class Configuration implements IConfig {
    
    private static Logger log = Logger.getLogger(Configuration.class);
    
    
    public class StringToMap extends TypeTransformer<String, Map<String, String>> {
        
        @Override
        public Map<String, String> transform( String input ) {
            Map<String, String> map = new HashMap<String, String>();
            if (!"".equals( input )) {
                String[] entries = input.split( "," );
                for (String entry : entries) {
                    String[] split = entry.split( "->" );
                    map.put( split[0], split[1] );
                }
            }
            return map;
        }
    }
    
    
	@Override
	public void initialize() {
	    // disable the default index menu of the base webapp
	    // we still have to use the option "indexing=true" to enable elastic search 
	    System.clearProperty( IKeys.INDEXING );
	    
	    // the results from this iPlug have to be grouped by its' domain and not by the iPlug ID
	    // see at IndexImpl.java in base-webapp for implementation
	    JettyStarter.getInstance().config.groupByUrl = true;
	}

	@PropertyValue("dir.instances")
	@DefaultValue("instances")
    private String dirInstances;

	@PropertyValue("db.id")
    @DefaultValue("iplug-se")
    public String databaseID;
	
	@PropertyValue("db.dir")
	@DefaultValue("database")
	public String databaseDir;
	
	@PropertyValue("transport.tcp.port")
    @DefaultValue("9300")
    public String esTransportTcpPort;
	
    @PropertyValue("nutch.call.java.options")
    @DefaultValue("-Dhadoop.log.file=hadoop.log -Dfile.encoding=UTF-8")
    @Separator(" ")
    public List<String> nutchCallJavaOptions;

    @PropertyValue("plugdescription.fields")
    @DefaultValue("")
    public List<String> fields;
    
    @PropertyValue("dependingFields")
    @DefaultValue("")
    public List<String> dependingFields;
    
    @TypeTransformers(Configuration.StringToMap.class)
    @PropertyValue("facetMapping")
    @DefaultValue("air->measure:air,radiation->measure:radiation,water->measure:water,misc->measure:misc,press->service:press,publication->service:publication,event->service:event")
    public Map<String, String> facetMap;
    
    @TypeTransformers(Configuration.StringToMap.class)
    @PropertyValue("queryFieldMapping")
    @DefaultValue("topic:air->measure:air,topic:radiation->measure:radiation,topic:water->measure:water,topic:misc->measure:misc,topic:press->service:press,topic:publication->service:publication,topic:event->service:event")
    public Map<String, String> queryFieldMap;
    
	
	@Override
    public void addPlugdescriptionValues( PlugdescriptionCommandObject pdObject ) {
        log.info("Add iPlug specific properties into plugdescription.");
	    
	    pdObject.put( "iPlugClass", "de.ingrid.iplug.se.SEIPlug" );

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
            if (field != null && !field.isEmpty() && !pdFields.contains(field)) {
                pdObject.addField( field );
            }
        }
    }

    @Override
    public void setPropertiesFromPlugdescription( Properties props, PlugdescriptionCommandObject pd ) {
        props.setProperty( "db.dir", this.databaseDir );
        props.setProperty( "dir.instances", this.dirInstances );
        
        // write elastic search properties to separate configuration
        // TODO: refactor this code to make an easy function, by putting it into
        // the base-webapp!
        Properties p = new Properties();
        try {
            // check for elastic search settings in classpath, which works
            // during development
            // and production
            Resource resource = new ClassPathResource( "/elasticsearch.properties" );
            if (resource.exists()) {
                p.load( resource.getInputStream() );
            } else {
                // create file if it does not exist yet
                // use the location of the production environment!
                resource = new FileSystemResource( "conf/elasticsearch.properties" );
            }
            //p.put( "http.port", esHttpPort );
            OutputStream os = new FileOutputStream( resource.getFile() ); 
            p.store( os, "Override configuration written by the application" );
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getInstancesDir() {
        return dirInstances;
    }
    
    public void setInstancesDir(String dir) {
        this.dirInstances = dir;
    }
    
    public Map<String, String> getElasticSearchSettings() {
        Map<String,String> map = new HashMap<String, String>();
        //map.put( "", "" )
        return map;
    }
    
}
