package de.ingrid.iplug.se.urlmaintenance.parse;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.ingrid.iplug.se.urlmaintenance.persistence.model.Url;

public class UrlContainer implements Externalizable {

  public static enum UrlType {
    WEB, CATALOG
  }

  private boolean _valid = false;

  private UrlType _urlType;

  private Serializable _providerId;

  private Map<String, Map<String, Set<String>>> _metadatas = new HashMap<String, Map<String, Set<String>>>();

  private Url _startUrl;

  private List<Url> _whiteUrls = new ArrayList<Url>();

  private List<Url> _blackUrls = new ArrayList<Url>();

  public void setStartUrl(final Url startUrl) {
    _startUrl = startUrl;
  }

  public Url getStartUrl() {
    return _startUrl;
  }

  public void setWhiteUrls(final List<Url> whiteUrls) {
    _whiteUrls = whiteUrls;
  }

  public void addWhiteUrl(final Url String) {
    _whiteUrls.add(String);
  }

  public List<Url> getWhiteUrls() {
    return _whiteUrls;
  }

  public List<Url> getBlackUrls() {
    return _blackUrls;
  }

  public void setBlackUrls(final List<Url> blackUrls) {
    _blackUrls = blackUrls;
  }

  public void addBlackUrl(final Url String) {
    _blackUrls.add(String);
  }

  public Serializable getProviderId() {
    return _providerId;
  }

  public void setProviderId(final Serializable providerId) {
    _providerId = providerId;
  }

  public Map<String, Map<String, Set<String>>> getMetadatas() {
    return _metadatas;
  }

  public void setMetadatas(final Map<String, Map<String, Set<String>>> metadatas) {
    _metadatas = metadatas;
  }

  public void addMetadata(final Url url, final String key, final String value) {
    if (!_metadatas.containsKey(url.getUrl())) {
      _metadatas.put(url.getUrl(), new HashMap<String, Set<String>>());
    }

    final Map<String, Set<String>> keyValues = _metadatas.get(url.getUrl());
    if (!keyValues.containsKey(key)) {
      keyValues.put(key, new HashSet<String>());
    }

    final Set<String> values = keyValues.get(key);
    values.add(value);
  }

  public boolean isValid() {
    return _valid;
  }

  public void setValid(final boolean valid) {
    _valid = valid;
  }

  public UrlType getUrlType() {
    return _urlType;
  }

  public void setUrlType(final UrlType urlType) {
    _urlType = urlType;
  }

  @Override
  @SuppressWarnings("unchecked")
  public void readExternal(final ObjectInput in) throws IOException, ClassNotFoundException {
    _valid = in.readBoolean();
    _urlType = (UrlType) in.readObject();
    _providerId = (Serializable) in.readObject();
    _metadatas = (Map<String, Map<String, Set<String>>>) in.readObject();
    _startUrl = new Url();
    _startUrl.setUrl(in.readUTF());
    int size = in.readInt();
    for (int i = 0; i < size; i++) {
      final Url url = new Url();
      url.setUrl(in.readUTF());
      _whiteUrls.add(url);
    }
    size = in.readInt();
    for (int i = 0; i < size; i++) {
      final Url url = new Url();
      url.setUrl(in.readUTF());
      _blackUrls.add(url);
    }
  }

  @Override
  public void writeExternal(final ObjectOutput out) throws IOException {
    out.writeBoolean(_valid);
    out.writeObject(_urlType);
    out.writeObject(_providerId);
    out.writeObject(_metadatas);
    out.writeUTF(_startUrl.getUrl());
    out.writeInt(_whiteUrls.size());
    for (final Url whiteUrl : _whiteUrls) {
      out.writeUTF(whiteUrl.getUrl());
    }
    out.writeInt(_blackUrls.size());
    for (final Url blackUrl : _blackUrls) {
      out.writeUTF(blackUrl.getUrl());
    }
  }
}
