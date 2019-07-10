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