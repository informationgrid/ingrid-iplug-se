/*
 * **************************************************-
 * ingrid-iplug-se-nutch
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
