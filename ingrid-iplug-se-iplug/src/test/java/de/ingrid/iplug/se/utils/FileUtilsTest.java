/*-
 * **************************************************-
 * ingrid-iplug-se-iplug
 * ==================================================
 * Copyright (C) 2014 - 2022 wemove digital solutions GmbH
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

import org.junit.jupiter.api.Test;

import java.net.MalformedURLException;
import java.net.URISyntaxException;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;

public class FileUtilsTest {

    @Test
    public void checkForRegularExpressions() {

        assertThat(FileUtils.checkForRegularExpressions("http://domain.de/.*"), is("http://domain.de/\\.\\*"));
        assertThat(FileUtils.checkForRegularExpressions("http://www.domain.de/Fön/Frisur?muß=ja&preis=groß#Außzüge"), is("http://www.domain.de/F%C3%B6n/Frisur\\?mu%C3%9F=ja&preis=gro%C3%9F#Au%C3%9Fz%C3%BCge"));

        assertThat(FileUtils.checkForRegularExpressions("http://www.schönheit.de/"), is("http://www.xn--schnheit-p4a.de/"));

        assertThat(FileUtils.checkForRegularExpressions("http://www.süß.de/Fön/Frisur?muß=ja&preis=groß#Außzüge"), is("http://www.xn--s-qfa0g.de/F%C3%B6n/Frisur\\?mu%C3%9F=ja&preis=gro%C3%9F#Au%C3%9Fz%C3%BCge"));
    }

    @Test
    public void getIdnUrlWithEncodedPath() throws MalformedURLException, URISyntaxException {
        assertThat(FileUtils.checkForRegularExpressions("http://www.geilenkirchen.de/stadtplanung/bauleitplanung/rechtskraeftige-bauleitpl%C3%A4ne/"), is("http://www.geilenkirchen.de/stadtplanung/bauleitplanung/rechtskraeftige-bauleitpl%C3%A4ne/"));
        assertThat(FileUtils.checkForRegularExpressions("http://www.geilenkirchen.de/stadtplanung/bauleitplanung/rechtskraeftige-bauleitpläne/"), is("http://www.geilenkirchen.de/stadtplanung/bauleitplanung/rechtskraeftige-bauleitpl%C3%A4ne/"));

        assertThat(FileUtils.checkForRegularExpressions("http://www.schönheit.de:8800/"), is("http://www.xn--schnheit-p4a.de:8800/"));
        assertThat(FileUtils.checkForRegularExpressions("https://www.schönheit.de/"), is("https://www.xn--schnheit-p4a.de/"));
    }
}
