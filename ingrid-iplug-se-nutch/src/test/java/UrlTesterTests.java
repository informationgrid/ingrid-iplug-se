import org.apache.hadoop.util.ToolRunner;
import org.apache.nutch.util.NutchConfiguration;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import de.ingrid.iplug.se.nutch.analysis.UrlTester;

/**
 * 
 */

/**
 * @author joachim
 * 
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class UrlTesterTests {

    @Test
    public void testExistingUrl() throws Exception {
        ToolRunner.run(NutchConfiguration.create(), new UrlTester(), new String[] { "http://www.google.de/" });
    }


    @Test
    public void testRedirectUrl() throws Exception {
        ToolRunner.run(NutchConfiguration.create(), new UrlTester(), new String[] { "http://www.wemove.com/" });
    }

    @Test
    public void testUnknownUrl() throws Exception {
        ToolRunner.run(NutchConfiguration.create(), new UrlTester(), new String[] { "http://www.wemove.com/unknown_url" });
    }

    @Test
    public void testUnknownHost() throws Exception {
        ToolRunner.run(NutchConfiguration.create(), new UrlTester(), new String[] { "http://www.qiwueqwewmwcueiwocq.com/" });
    }

    @Test
    public void testCrawlDelayHost() throws Exception {
        ToolRunner.run(NutchConfiguration.create(), new UrlTester(), new String[] { "http://www.umweltbundesamt.de/" });
    }
}
