package de.ingrid.iplug.se.crawl.sns;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.util.HashSet;
import java.util.Set;

import junit.framework.TestCase;

import org.apache.hadoop.io.Text;

public class CompressedSnsDataTest extends TestCase {

  public void testWrite() throws Exception {
    CompressedSnsData snsData = new CompressedSnsData();

    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    DataOutput dataOutputStream = new DataOutputStream(byteArrayOutputStream);

    Set<Text> buzzwords = new HashSet<Text>();
    buzzwords.add(new Text("luft"));
    buzzwords.add(new Text("wasser"));
    buzzwords.add(new Text("erde"));
    String text = "dsaaaaaaaaaaaaaa dsdsadsa  zvcxxcvcxvx  fdsfdsfrgdthgfhf";
    for (int i = 0; i < 5; i++) {
      snsData.resetValues();
      snsData.setBuzzwords(buzzwords);
      snsData.setText(new Text(text));
      snsData.setCoordinatesFound(true);
      snsData.setX1(new Text("" + i));
      snsData.setX2(new Text("" + i + i));
      snsData.setY1(new Text("" + i * 2));
      snsData.setY2(new Text("" + i * 3));
      Set<Text> codes = new HashSet<Text>();
      Set<Text> topcIds = new HashSet<Text>();
      for (int k = 0; k < i; k++) {
        topcIds.add(new Text("" + k * 2));
        codes.add(new Text("" + k));
      }
      snsData.setCommunityCodes(codes);
      snsData.setTopicIds(topcIds);
      snsData.write(dataOutputStream);
    }

    byte[] snsDataBlob = byteArrayOutputStream.toByteArray();
    ByteArrayInputStream inputStream = new ByteArrayInputStream(snsDataBlob);
    DataInput dataInputStream = new DataInputStream(inputStream);
    for (int i = 0; i < 5; i++) {

      snsData.resetValues();
      snsData.readFields(dataInputStream);
      assertNotNull(snsData.getX1());
      if (i == 0) {
        continue;
      }
      assertNotNull(snsData.getCommunityCodes());
      assertNotNull(snsData.getTopicIds());
      assertFalse(snsData.getCommunityCodes().isEmpty());
      assertFalse(snsData.getTopicIds().isEmpty());
      assertTrue(snsData.getBuzzwords().containsAll(buzzwords));
    }
  }
}
