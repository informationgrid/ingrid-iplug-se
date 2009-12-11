package de.ingrid.iplug.se.crawl.sns;

import org.apache.nutch.parse.ParseData;
import org.apache.nutch.parse.ParseImpl;
import org.apache.nutch.parse.ParseText;

public class SnsParseImpl extends ParseImpl {

  private CompressedSnsData _snsData;

  public SnsParseImpl(ParseText text, ParseData data, CompressedSnsData snsData) {
    super(text, data);
    _snsData = snsData;
  }

  public CompressedSnsData getSnsData() {
    return _snsData;
  }
}
