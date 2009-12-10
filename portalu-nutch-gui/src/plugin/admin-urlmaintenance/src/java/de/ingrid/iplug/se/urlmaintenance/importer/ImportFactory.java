package de.ingrid.iplug.se.urlmaintenance.importer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.springframework.web.multipart.MultipartFile;

import de.ingrid.iplug.se.urlmaintenance.parse.CsvParser;
import de.ingrid.iplug.se.urlmaintenance.parse.GoogleSiteXmlParser;
import de.ingrid.iplug.se.urlmaintenance.parse.IUrlFileParser;
import de.ingrid.iplug.se.urlmaintenance.parse.UrlContainer.UrlType;
import de.ingrid.iplug.se.urlmaintenance.persistence.dao.IProviderDao;

public class ImportFactory {

  private final UploadCommand _uploadCommand;
  private final IProviderDao _providerDao;

  public ImportFactory(final UploadCommand uploadCommand, final IProviderDao providerDao) {
    _uploadCommand = uploadCommand;
    _providerDao = providerDao;
  }

  public IUrlFileParser findFileParser() throws Exception {
    final String name = _uploadCommand.getFile().getOriginalFilename();
    if (name.endsWith(".xml")) {
      return new GoogleSiteXmlParser(_uploadCommand.getType(), _uploadCommand.getProviderId());
    } else if (name.endsWith(".csv")) {
      return new CsvParser(_uploadCommand.getType(), _uploadCommand.getProviderId());
    }
    return null;
  }

  public IUrlValidator findUrlValidator() {
    final UrlType type = _uploadCommand.getType();
    if (type == UrlType.WEB) {
      return new WebUrlValidator(_providerDao);
    } else if (type == UrlType.CATALOG) {
      return new CatalogUrlValidator(_providerDao);
    }
    return null;
  }

  public File saveFile() throws IOException {
    final MultipartFile in = _uploadCommand.getFile();
    final File out = createTempOutputFile();

    final File file = new File(out, in.getOriginalFilename());
    if (!file.exists()) {
      file.createNewFile();
    }
    final FileOutputStream fos = new FileOutputStream(file, false);
    final InputStream is = in.getInputStream();

    final byte[] buffer = new byte[1024];
    int read = -1;
    while ((read = is.read(buffer, 0, 1024)) > -1) {
      fos.write(buffer, 0, read);
    }
    fos.close();
    is.close();

    return file;
  }

  private File createTempOutputFile() {
    final File file = new File(System.getProperty("java.io.tmpdir"), "urlimport-" + System.currentTimeMillis());
    file.mkdirs();
    return file;
  }
}
