package de.ingrid.iplug.se.crawl.sns;

import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.RecordReader;

public class SnsInMemoryRecordReader implements RecordReader<Text, CompressedSnsData> {

  private static final Log LOGGER = LogFactory.getLog(SnsInMemoryRecordReader.class);

  // TODO make configurable
  private static final int MAX_SIZE = 500;

  private long _skippedUrls = 0;

  private int _pos = 0;

  private Map<Text, String> _map = new TreeMap<Text, String>();

  private boolean _close;

  public void close() throws IOException {
    synchronized (_map) {
      _map.notify();
    }
    _close = true;
  }

  public long getPos() throws IOException {
    return _pos;
  }

  @Override
  public boolean next(Text key, CompressedSnsData value) throws IOException {
    if (_close) {
      LOGGER.info("Sns Analyzing was stopped. The rest of the urls will be skipped: " + _map.size());
      return false;
    }
    synchronized (_map) {
      // wait add-notify or close-notify
      if (_map.isEmpty()) {
        try {
          _map.wait();
        } catch (InterruptedException e) {
          throw new IOException(e.getMessage());
        }
      }
      // read if add-notify was called
      if (!_map.isEmpty()) {
        Text nextKey = _map.keySet().iterator().next();
        String nextValue = _map.remove(nextKey);
        key.set(nextKey);
        value.setText(new Text(nextValue));
        _pos++;
      }
    }
    if (_close) {
      LOGGER.info("Sns Analyzing was stopped. The rest of the urls will be skipped: " + _map.size());
    }
    return !_close;
  }

  public void add(Text url, String text) {
    synchronized (_map) {
      if (_map.size() < MAX_SIZE) {
        _map.put(url, text);
      } else {
        _skippedUrls++;
        LOGGER.warn("Do not put new url [" + url + "] into map because max size is exceeded: " + _map.size());
        LOGGER.warn("Number of skipped urls: " + _skippedUrls);
      }
      _map.notify();
    }
  }

  public static void main(String[] args) throws Exception {

    final SnsInMemoryRecordReader recordReader = new SnsInMemoryRecordReader();
    Runnable runnable = new Runnable() {
      public void run() {
        Text url = new Text();
        CompressedSnsData data = new CompressedSnsData();
        try {
          while (recordReader.next(url, data)) {
            // do nothing her just pull key and value from reader's map
            ;
          }
        } catch (IOException e) {
          LOGGER.error("Internal error:", e);
        }
      }
    };

    new Thread(runnable).start();

    for (int i = 0; i < 10; i++) {
      String string = "foo_" + i;
      String string2 = "bar_" + i;
      recordReader.add(new Text(string), string2);
      // Thread.sleep(1000);
    }
    recordReader.close();
  }

  @Override
  public Text createKey() {
    return null;
  }

  @Override
  public CompressedSnsData createValue() {
    return null;
  }

  @Override
  public float getProgress() throws IOException {
    return 0;
  }

}
