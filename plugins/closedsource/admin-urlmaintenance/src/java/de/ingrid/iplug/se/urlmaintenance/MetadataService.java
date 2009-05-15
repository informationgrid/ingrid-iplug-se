package de.ingrid.iplug.se.urlmaintenance;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.springframework.stereotype.Service;

import de.ingrid.iplug.se.urlmaintenance.persistence.model.Metadata;

@Service
public class MetadataService {

  private Properties _properties;

  public MetadataService() throws IOException {
    InputStream stream = MetadataService.class
        .getResourceAsStream("/metadatas.properties");
    _properties = new Properties();
    _properties.load(stream);

  }

  public List<Metadata> getLang() {
    return getMetadatasAsList("lang");
  }

  public List<Metadata> getDatatypes() {
    return getMetadatasAsList("datatype");
  }

  public List<Metadata> getTopics() {
    return getMetadatasAsList("topics");
  }

  public List<Metadata> getFunctCategory() {
    return getMetadatasAsList("funct_category");
  }

  public List<Metadata> getMeasure() {
    return getMetadatasAsList("measure");
  }

  public List<Metadata> getService() {
    return getMetadatasAsList("service");
  }

  private List<Metadata> getMetadatasAsList(String metadataKey) {
    List<String> list = getPropertiesAsList(metadataKey);
    List<Metadata> arrayList = new ArrayList<Metadata>();
    for (String string : list) {
      Metadata metadata = new Metadata();
      metadata.setMetadataKey(metadataKey);
      metadata.setMetadataValue(string);
      arrayList.add(metadata);
    }
    return arrayList;
  }

  private List<String> getPropertiesAsList(String property) {
    String string = (String) _properties.get(property);
    String[] splits = string.split(",");
    return Arrays.asList(splits);
  }
}
