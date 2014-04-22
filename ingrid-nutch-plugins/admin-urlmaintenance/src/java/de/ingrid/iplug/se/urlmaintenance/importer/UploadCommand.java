package de.ingrid.iplug.se.urlmaintenance.importer;

import java.io.Serializable;

import org.springframework.web.multipart.MultipartFile;

import de.ingrid.iplug.se.urlmaintenance.parse.UrlContainer.UrlType;

public class UploadCommand {

  private MultipartFile _file;

  private UrlType _type;

  private Serializable _providerId;

  public void setFile(final MultipartFile file) {
    _file = file;
  }

  public MultipartFile getFile() {
    return _file;
  }

  public void setType(final UrlType type) {
    _type = type;
  }

  public UrlType getType() {
    return _type;
  }

  public void setProviderId(final Serializable providerId) {
    _providerId = providerId;
  }

  public Serializable getProviderId() {
    return _providerId;
  }

}
