package de.ingrid.iplug.se.elasticsearch;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.nio.file.Files;
import java.nio.file.Paths;

import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.client.Client;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

import de.ingrid.iplug.se.Index;
import de.ingrid.iplug.se.elasticsearch.bean.ElasticsearchNodeFactoryBean;
import de.ingrid.utils.IngridHits;

public class IndexImplTest {
	
	static Index index = null;
	private ElasticsearchNodeFactoryBean elastic;

	@Before
	public void setUp() throws Exception {
		if (index == null) {
			elastic = new ElasticsearchNodeFactoryBean();
			elastic.setConfigLocation( new ClassPathResource( "elasticsearch_memory.yml" ) );
			elastic.afterPropertiesSet();
			index = new IndexImpl( elastic );
			
			prepareIndex();
		}
	}



	@AfterClass
	public static void tearDown() throws Exception {
		index.close();
	}

	@Test
	public void testSearch() {
		assertThat(index, not( is( nullValue() ) ));
		
		IngridHits search = index.search( null, 0, 10 );
		assertThat(search, not( is( nullValue() ) ));
		assertThat(search.getHits().length, is( 10 ));
	}

	@Test
	public void testGetDetail() {
		fail( "Not yet implemented" );
	}

	@Test
	public void testDeleteUrl() {
		fail( "Not yet implemented" );
	}

	
	
	private void prepareIndex() throws Exception {
		Client client = elastic.getObject().client();
		ClassPathResource resource = new ClassPathResource( "data/webUrls.json" );
		
		byte[] urlsData = Files.readAllBytes( Paths.get( resource.getURI() ) );
		
		BulkResponse indexResponse = client.prepareBulk()
		        .add(urlsData, 0, urlsData.length, true)
		        .execute()
		        .actionGet();
		
		//System.out.println( indexResponse );
	}
}
