package org.apache.nutch.crawl.bw;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import junit.framework.TestCase;

import org.apache.hadoop.io.ObjectWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.nutch.crawl.bw.BWUpdateDb.BwReducer;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class BWUpdateDbTest extends TestCase {

  private BWPatterns _bwPatterns;
  private BwReducer _bwReducer;
  private Collection<ObjectWritable> _objectWritables = new ArrayList<ObjectWritable>();

  @Mock
  private OutputCollector<HostTypeKey, ObjectWritable> _outputCollector;
  
  protected void setUp() throws Exception {
    
    MockitoAnnotations.initMocks(this);
    
    ObjectWritable objectWritable = mock(ObjectWritable.class);
    _bwPatterns = new BWPatterns(new Text[0], new Text[0]);
    when(objectWritable.get()).thenReturn(_bwPatterns);
    _objectWritables.add(objectWritable);

    _bwReducer = new BWUpdateDb.BwReducer();
  }

  protected void tearDown() {
  }

  public void testBwReducer_Reduce_WithOnlyWhiteList_Pass() throws IOException{
    addToWhiteList("http://lucene.apache.org/Nutch/");
    
    ObjectWritable objectWritable = mock(ObjectWritable.class);
    when(objectWritable.get()).thenReturn(new BWUpdateDb.Entry(new Text("http://lucene.apache.org/nutch/index.html"), null));
    _objectWritables.add(objectWritable);
    
    Iterator<ObjectWritable> iterator = _objectWritables.iterator();
    HostTypeKey key = mock(HostTypeKey.class);
    
    // init _patterns attribute
    _bwReducer.reduce(key, iterator, _outputCollector, null);
    _bwReducer.reduce(key, iterator, _outputCollector, null);
    
    // expected is the url in the output
    verify(_outputCollector).collect(key, objectWritable);
  }

  public void testBwReducer_Reduce_WithOnlyWhiteList_DontPass() throws IOException{
    addToWhiteList("http://lucene.apache.org/Lucy/");
    
    ObjectWritable objectWritable = mock(ObjectWritable.class);
    when(objectWritable.get()).thenReturn(new BWUpdateDb.Entry(new Text("http://lucene.apache.org/nutch/index.html"), null));
    _objectWritables.add(objectWritable);
    
    Iterator<ObjectWritable> iterator = _objectWritables.iterator();
    HostTypeKey key = mock(HostTypeKey.class);
    
    // init _patterns attribute
    _bwReducer.reduce(key, iterator, _outputCollector, null);
    _bwReducer.reduce(key, iterator, _outputCollector, null);
    
    // expected is the url in the output
    verify(_outputCollector, never()).collect(key, objectWritable);
  }

  public void testBwReducer_Reduce_WithOnlyBlackList_Pass() throws IOException{
    addToBlackList("http://lucene.apache.org/nutch/");
    
    ObjectWritable objectWritable = mock(ObjectWritable.class);
    when(objectWritable.get()).thenReturn(new BWUpdateDb.Entry(new Text("http://lucene.apache.org/Nutch/index.html"), null));
    _objectWritables.add(objectWritable);
    
    Iterator<ObjectWritable> iterator = _objectWritables.iterator();
    HostTypeKey key = mock(HostTypeKey.class);
    
    // init _patterns attribute
    _bwReducer.reduce(key, iterator, _outputCollector, null);
    _bwReducer.reduce(key, iterator, _outputCollector, null);
    
    // expected is the url in the output
    verify(_outputCollector, never()).collect(key, objectWritable);
  }

  public void testBwReducer_Reduce_WithOnlyBlackList_DontPass() throws IOException{
    addToBlackList("http://lucene.apache.org/lucy/");
    
    ObjectWritable objectWritable = mock(ObjectWritable.class);
    when(objectWritable.get()).thenReturn(new BWUpdateDb.Entry(new Text("http://lucene.apache.org/Nutch/index.html"), null));
    _objectWritables.add(objectWritable);
    
    Iterator<ObjectWritable> iterator = _objectWritables.iterator();
    HostTypeKey key = mock(HostTypeKey.class);
    
    // init _patterns attribute
    _bwReducer.reduce(key, iterator, _outputCollector, null);
    _bwReducer.reduce(key, iterator, _outputCollector, null);
    
    // expected is the url in the output
    verify(_outputCollector).collect(key, objectWritable);
  }

  public void testBwReducer_Reduce_WithRegexInWhiteList() throws IOException{
    addToWhiteList("http://lucene.apache.org/nutch/Tutorial[0-9]?");
    
    ObjectWritable urlPass1 = addUrl("http://lucene.apache.org/nutch/tutorial.html");
    ObjectWritable urlPass2 = addUrl("http://lucene.apache.org/nutch/tutorial8.html");
    ObjectWritable urlPass3 = addUrl("http://lucene.apache.org/nutch/Bot.html");
    ObjectWritable urlPass4 = addUrl("http://lucene.apache.org/nutch/i18n.html");
    
    Iterator<ObjectWritable> iterator = _objectWritables.iterator();
    HostTypeKey key = mock(HostTypeKey.class);
    
    // init _patterns attribute
    _bwReducer.reduce(key, iterator, _outputCollector, null);
    // reduce urls
    _bwReducer.reduce(key, iterator, _outputCollector, null);
    
    // expected is the url in the output
    verify(_outputCollector).collect(key, urlPass1);
    verify(_outputCollector).collect(key, urlPass2);
    verify(_outputCollector, never()).collect(key, urlPass3);
    verify(_outputCollector, never()).collect(key, urlPass4);
  }

  public void testBwReducer_Reduce_WithRegexInBlackList() throws IOException{
    // exclude tutorial sides that have a number in name
    addToBlackList("http://lucene.apache.org/nutch/Tutorial[0-9]");
    
    ObjectWritable urlPass1 = addUrl("http://lucene.apache.org/nutch/tutorial.html");
    ObjectWritable urlPass2 = addUrl("http://lucene.apache.org/nutch/tutorial8.html");
    ObjectWritable urlPass3 = addUrl("http://lucene.apache.org/nutch/Bot.html");
    ObjectWritable urlPass4 = addUrl("http://lucene.apache.org/nutch/i18n.html");
    
    Iterator<ObjectWritable> iterator = _objectWritables.iterator();
    HostTypeKey key = mock(HostTypeKey.class);
    
    // init _patterns attribute
    _bwReducer.reduce(key, iterator, _outputCollector, null);
    // reduce urls
    _bwReducer.reduce(key, iterator, _outputCollector, null);
    
    // expected is the url in the output
    verify(_outputCollector).collect(key, urlPass1);
    verify(_outputCollector, never()).collect(key, urlPass2);
    verify(_outputCollector).collect(key, urlPass3);
    verify(_outputCollector).collect(key, urlPass4);
  }

  private ObjectWritable addUrl(String url) {
    ObjectWritable urlPass = mock(ObjectWritable.class);
    when(urlPass.get()).thenReturn(new BWUpdateDb.Entry(new Text(url), null));
    _objectWritables.add(urlPass);
    return urlPass;
  }

  private void addToBlackList(String string) {
    List<Text> list = _bwPatterns.getNegative();
    list.add(new Text(string));
    _bwPatterns.setNegative(list);
  }

  private void addToWhiteList(String string) {
    List<Text> list = _bwPatterns.getPositive();
    list.add(new Text(string));
    _bwPatterns.setPositive(list);
  }
}
