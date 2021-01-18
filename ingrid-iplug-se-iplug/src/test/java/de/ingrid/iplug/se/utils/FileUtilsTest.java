/*-
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

import org.junit.Assert;
import org.junit.Test;

import java.net.MalformedURLException;
import java.net.URISyntaxException;

public class FileUtilsTest {

    @Test
    public void checkForRegularExpressions() {

        Assert.assertEquals("http://domain.de/\\.\\*", FileUtils.checkForRegularExpressions("http://domain.de/.*"));
        Assert.assertEquals("http://www.domain.de/F%C3%B6n/Frisur\\?mu%C3%9F=ja&preis=gro%C3%9F#Au%C3%9Fz%C3%BCge", FileUtils.checkForRegularExpressions("http://www.domain.de/Fön/Frisur?muß=ja&preis=groß#Außzüge"));

        Assert.assertEquals("http://www.xn--schnheit-p4a.de/", FileUtils.checkForRegularExpressions("http://www.schönheit.de/"));

        Assert.assertEquals("http://www.xn--s-qfa0g.de/F%C3%B6n/Frisur\\?mu%C3%9F=ja&preis=gro%C3%9F#Au%C3%9Fz%C3%BCge", FileUtils.checkForRegularExpressions("http://www.süß.de/Fön/Frisur?muß=ja&preis=groß#Außzüge"));
    }

    @Test
    public void getIdnUrlWithEncodedPath() throws MalformedURLException, URISyntaxException {
        Assert.assertEquals( "http://www.geilenkirchen.de/stadtplanung/bauleitplanung/rechtskraeftige-bauleitpl%C3%A4ne/", FileUtils.checkForRegularExpressions("http://www.geilenkirchen.de/stadtplanung/bauleitplanung/rechtskraeftige-bauleitpl%C3%A4ne/"));
        Assert.assertEquals( "http://www.geilenkirchen.de/stadtplanung/bauleitplanung/rechtskraeftige-bauleitpl%C3%A4ne/", FileUtils.checkForRegularExpressions("http://www.geilenkirchen.de/stadtplanung/bauleitplanung/rechtskraeftige-bauleitpläne/"));

        Assert.assertEquals("http://www.xn--schnheit-p4a.de:8800/", FileUtils.checkForRegularExpressions("http://www.schönheit.de:8800/"));
        Assert.assertEquals("https://www.xn--schnheit-p4a.de/", FileUtils.checkForRegularExpressions("https://www.schönheit.de/"));
    }
}
