/*
 * **************************************************-
 * ingrid-iplug-se-iplug
 * ==================================================
 * Copyright (C) 2014 - 2021 wemove digital solutions GmbH
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
package de.ingrid.iplug.se.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

import org.elasticsearch.action.admin.indices.exists.types.TypesExistsRequest;
import org.elasticsearch.client.Client;
import org.springframework.core.io.ClassPathResource;

import de.ingrid.admin.JettyStarter;

@Deprecated
public class ElasticSearchUtils {

    public static boolean typeExists(String type, Client client) {
        TypesExistsRequest typeRequest = new TypesExistsRequest( new String[]{ JettyStarter.baseConfig.index }, type );
        boolean typeExists = client.admin().indices().typesExists( typeRequest ).actionGet().isExists();
        return typeExists;
    }
    
    public static void createIndexType(String type, Client client) throws Exception {
        String indexName = JettyStarter.baseConfig.index;
        client.admin().indices().preparePutMapping().setIndices( indexName )
            .setType( type )
            .setSource( getMappingSource() )
            .execute()
            .actionGet();
    }
    
    private static String getMappingSource() throws IOException {
        ClassPathResource resource = new ClassPathResource( "mappingProperties.json" );
        BufferedReader in = new BufferedReader(new InputStreamReader( resource.getInputStream(), Charset.defaultCharset() ));
        String line = null;

        StringBuilder mappingSource = new StringBuilder();
        while((line = in.readLine()) != null) {
            mappingSource.append(line);
        }

        return mappingSource.toString();
    }

    public static void deleteType(String name, Client client) {
        /*client.admin().indices().prepareDeleteMapping( baseConfig.index )
            .setType( name )
            .execute()
            .actionGet();*/
        
    }
    
}
