package de.ingrid.iplug.se.utils;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import org.elasticsearch.action.admin.indices.exists.types.TypesExistsRequest;
import org.elasticsearch.client.Client;
import org.springframework.core.io.ClassPathResource;

import de.ingrid.iplug.se.SEIPlug;

public class ElasticSearchUtils {

    public static boolean typeExists(String type, Client client) {
        TypesExistsRequest typeRequest = new TypesExistsRequest( new String[]{ SEIPlug.conf.index }, type );
        boolean typeExists = client.admin().indices().typesExists( typeRequest ).actionGet().isExists();
        return typeExists;
    }
    
    public static void createIndexType(String type, Client client) throws Exception {
        String indexName = SEIPlug.conf.index;
        client.admin().indices().preparePutMapping().setIndices( indexName )
            .setType( type )
            .setSource( getMappingSource() )
            .execute()
            .actionGet();
    }
    
    private static String getMappingSource() throws IOException {
        ClassPathResource resource = new ClassPathResource( "mappingProperties.json" );
        List<String> urlsData = Files.readAllLines( Paths.get( resource.getURI() ), Charset.defaultCharset() );
        String mappingSource = "";
        for (String line : urlsData) {
            mappingSource  += line;
        }
        return mappingSource;
    }

    public static void deleteType(String name, Client client) {
        client.admin().indices().prepareDeleteMapping( SEIPlug.conf.index )
            .setType( name )
            .execute()
            .actionGet();
        
    }
    
}
