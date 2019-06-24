package de.ingrid.iplug.se.utils;

import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.*;

public class FileUtilsTest {

    @Test
    public void checkForRegularExpressions() {

        Assert.assertEquals("http://domain.de/\\.\\*", FileUtils.checkForRegularExpressions("http://domain.de/.*"));
        Assert.assertEquals("http://www.domain.de/F%C3%B6n/Frisur\\?mu%C3%9F=ja&preis=gro%C3%9F#Au%C3%9Fz%C3%BCge", FileUtils.checkForRegularExpressions("http://www.domain.de/Fön/Frisur?muß=ja&preis=groß#Außzüge"));

        Assert.assertEquals("http://www.xn--schnheit-p4a.de/", FileUtils.checkForRegularExpressions("http://www.schönheit.de/"));

        Assert.assertEquals("http://www.xn--sss-hoa.de/F%C3%B6n/Frisur\\?mu%C3%9F=ja&preis=gro%C3%9F#Au%C3%9Fz%C3%BCge", FileUtils.checkForRegularExpressions("http://www.süß.de/Fön/Frisur?muß=ja&preis=groß#Außzüge"));
    }
}