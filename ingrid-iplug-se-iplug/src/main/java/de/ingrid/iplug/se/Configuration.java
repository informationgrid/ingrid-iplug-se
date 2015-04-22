/*
 * **************************************************-
 * ingrid-iplug-se-iplug
 * ==================================================
 * Copyright (C) 2014 - 2015 wemove digital solutions GmbH
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

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.common.lucene.search.function.FieldValueFactorFunction.Modifier;
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
import de.ingrid.admin.command.PlugdescriptionCommandObject;
import de.ingrid.utils.PlugDescription;
import de.ingrid.utils.query.IngridQuery;

@PropertiesFiles( {"config", "elasticsearch"} )
@PropertyLocations(directories = {"conf"}, fromClassLoader = true)
public class Configuration implements IConfig {
    
    private static Logger log = Logger.getLogger(Configuration.class);
    
    public class StringToSearchType extends TypeTransformer<String, SearchType> {
        
        @Override
        public SearchType transform( String input ) {
            SearchType type;
            switch (input) {
            case "COUNT":
                type = SearchType.COUNT;
                break;
            case "DEFAULT":
                type = SearchType.DEFAULT;
                break;
            case "DFS_QUERY_AND_FETCH":
                type = SearchType.DFS_QUERY_AND_FETCH;
                break;
            case "DFS_QUERY_THEN_FETCH":
                type = SearchType.DFS_QUERY_THEN_FETCH;
                break;
            case "QUERY_AND_FETCH":
                type = SearchType.QUERY_AND_FETCH;
                break;
            case "QUERY_THEN_FETCH":
                type = SearchType.QUERY_THEN_FETCH;
                break;
            case "SCAN":
                type = SearchType.SCAN;
                break;
            default:
                log.error( "Unknown SearchType (" + input + "), using default one: DFS_QUERY_THEN_FETCH" );
                type = SearchType.DFS_QUERY_THEN_FETCH;
            }
            return type;
        }
        
    }
    
    public class StringToModifier extends TypeTransformer<String, Modifier> {
        
        @Override
        public Modifier transform( String input ) {
            Modifier modifier = null;
            switch (input) {
            case "none":
                modifier = Modifier.NONE;
                break;
            case "log":
                modifier = Modifier.LOG;
                break;
            case "log1p":
                modifier = Modifier.LOG1P;
                break;
            case "log2p":
                modifier = Modifier.LOG2P;
                break;
            case "ln":
                modifier = Modifier.LN;
                break;
            case "ln1p":
                modifier = Modifier.LN1P;
                break;
            case "ln2p":
                modifier = Modifier.LN2P;
                break;
            case "square":
                modifier = Modifier.SQUARE;
                break;
            case "sqrt":
                modifier = Modifier.SQRT;
                break;
            case "reciprocal":
                modifier = Modifier.RECIPROCAL;
                break;
            }
            return modifier;
        }
    }
    
	@Override
	public void initialize() {
	    // disable the default index menu of the base webapp
	    // we still have to use the option "indexing=true" to enable elastic search 
	    System.clearProperty( IKeys.INDEXING );
	}

    @TypeTransformers(Configuration.StringToSearchType.class)
    @PropertyValue("search.type")
    @DefaultValue("DEFAULT")
    public SearchType searchType;
	
	@PropertyValue("dir.instances")
	@DefaultValue("instances")
    private String dirInstances;

	@PropertyValue("db.id")
    @DefaultValue("iplug-se")
    public String databaseID;
	
	@PropertyValue("db.dir")
	@DefaultValue("database")
	public String databaseDir;
	
	@PropertyValue("http.port")
    @DefaultValue("9200")
    public String esHttpPort;
    
	@PropertyValue("transport.tcp.port")
    @DefaultValue("9300")
    public String esTransportTcpPort;

    @PropertyValue("http.host")
    @DefaultValue("localhost")
    public String esHttpHost;
    
    @PropertyValue("elastic.boost.field")
    @DefaultValue("doc_boost")
    public String esBoostField;
    
    @TypeTransformers(Configuration.StringToModifier.class)
    @PropertyValue("elastic.boost.modifier")
    @DefaultValue("log1p")
    public Modifier esBoostModifier;
    
    @PropertyValue("elastic.boost.factor")
    @DefaultValue("0.1")
    public float esBoostFactor;
    
    @PropertyValue("elastic.boost.mode")
    @DefaultValue("sum")
    public String esBoostMode;
	
	@PropertyValue("index.name")
	@DefaultValue("iplugse")
	public String index;
	
	@PropertyValue("instance.active")
	@DefaultValue("")
	public List<String> activeInstances;
	
    @PropertyValue("nutch.call.java.options")
    @DefaultValue("-Dhadoop.log.file=hadoop.log -Dfile.encoding=UTF-8")
    @Separator(" ")
    public List<String> nutchCallJavaOptions;

    @PropertyValue("plugdescription.fields")
    @DefaultValue("")
    public List<String> fields;
	
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
        props.setProperty( "instance.active", getActiveInstancesAsString() );        
        
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
            p.put( "http.port", esHttpPort );
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
    
    public String getActiveInstancesAsString() {
        return StringUtils.join( this.activeInstances, ',' );
    }

}
