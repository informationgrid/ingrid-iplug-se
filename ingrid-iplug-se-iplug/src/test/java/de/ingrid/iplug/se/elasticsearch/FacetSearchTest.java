package de.ingrid.iplug.se.elasticsearch;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import de.ingrid.admin.JettyStarter;
import de.ingrid.iplug.se.elasticsearch.bean.ElasticsearchNodeFactoryBean;
import de.ingrid.iplug.se.elasticsearch.converter.QueryConverter;
import de.ingrid.utils.IngridDocument;
import de.ingrid.utils.IngridHits;
import de.ingrid.utils.query.IngridQuery;

@RunWith(PowerMockRunner.class)
@PrepareForTest(JettyStarter.class)
public class FacetSearchTest  {

    @Mock JettyStarter jettyStarter;
    
    private IndexImpl index;
    private static ElasticsearchNodeFactoryBean elastic;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        elastic = Utils.setupES();
    }
    
    @Before
    public void initTest() throws Exception {
        QueryConverter qc = Utils.getQueryConverter( jettyStarter );
        index = new IndexImpl( elastic, qc, new FacetConverter() );
    }

    @Test
    public void facetteSearch() {
        IngridQuery ingridQuery = Utils.getIngridQuery( "wemove" );
        Utils.addDefaultFacets( ingridQuery );
        IngridHits search = index.search( ingridQuery , 0, 10 );
        assertThat( search, not( is( nullValue() ) ) );
        assertThat( search.getHits().length, is( 4 ) );
        
        IngridDocument facets = (IngridDocument) search.get("FACETS");
        assertThat( facets.size(), is( 5 ) );
        assertThat( facets.getLong( "partner:bund" ), is( 2l ) );
        assertThat( facets.getLong( "partner:bb" ), is( 1l ) );
        assertThat( facets.getLong( "partner:th" ), is( 1l ) );
        assertThat( facets.getLong( "after:April2014" ), is( 1l ) );
    }
}
