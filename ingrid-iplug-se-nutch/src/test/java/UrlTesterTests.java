/*
 * **************************************************-
 * ingrid-iplug-se-nutch
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
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.util.ToolRunner;
import org.apache.nutch.util.NutchConfiguration;

import de.ingrid.iplug.se.nutch.analysis.UrlTester;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

/**
 * 
 */

/**
 * @author joachim
 * 
 */
@TestMethodOrder(MethodOrderer.MethodName.class)
public class UrlTesterTests {

    @Test
    public void testExistingUrl() throws Exception {
        Configuration c = NutchConfiguration.create();
        c.set("plugin.folders", "build/apache-nutch-1.19/runtime/local/plugins");
        ToolRunner.run(c, new UrlTester(), new String[]{"https://www.google.de/"});
    }


    @Test
    public void testRedirectUrl() throws Exception {
        Configuration c = NutchConfiguration.create();
        c.set("plugin.folders", "build/apache-nutch-1.19/runtime/local/plugins");
        ToolRunner.run(c, new UrlTester(), new String[] { "https://www.wemove.com/" });
    }

    @Test
    public void testUnknownUrl() throws Exception {
        Configuration c = NutchConfiguration.create();
        c.set("plugin.folders", "build/apache-nutch-1.19/runtime/local/plugins");
        ToolRunner.run(c, new UrlTester(), new String[] { "https://www.wemove.com/unknown_url" });
    }

    @Test
    public void testUnknownHost() throws Exception {
        Configuration c = NutchConfiguration.create();
        c.set("plugin.folders", "build/apache-nutch-1.19/runtime/local/plugins");
        ToolRunner.run(c, new UrlTester(), new String[] { "https://www.qiwueqwewmwcueiwocq.com/" });
    }

    @Test
    public void testCrawlDelayHost() throws Exception {
        Configuration c = NutchConfiguration.create();
        c.set("plugin.folders", "build/apache-nutch-1.19/runtime/local/plugins");
        ToolRunner.run(c, new UrlTester(), new String[] { "https://www.umweltbundesamt.de/" });
    }
}
