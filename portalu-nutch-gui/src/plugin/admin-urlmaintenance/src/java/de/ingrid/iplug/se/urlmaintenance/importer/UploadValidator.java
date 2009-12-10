package de.ingrid.iplug.se.urlmaintenance.importer;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.web.multipart.MultipartFile;

public class UploadValidator {

  private static final long MAX_SIZE = 3145728;

  private final Set<String> _contentTypes = new HashSet<String>();

  public UploadValidator() {
    _contentTypes.add("application/xml");
    _contentTypes.add("application/text");
    _contentTypes.add("text/xml");
    _contentTypes.add("text/plain");
    _contentTypes.add(null);
  }

  public boolean validate(final MultipartFile file, final Map<String, String> errors) {
    int errorCount = 0;
    errorCount += validateFilename(file.getOriginalFilename(), errors);
    errorCount += validateContentType(file.getContentType(), errors);
    errorCount += validateSize(file.getSize(), errors);
    return errorCount == 0;
  }

  private int validateSize(final long size, final Map<String, String> errors) {
    int errorCount = 0;
    if (size > MAX_SIZE) {
      errors.put("size.invalid", "" + size);
      errorCount++;
    }
    return errorCount;
  }

  private int validateContentType(final String contentType, final Map<String, String> errors) {
    int errorCount = 0;
    if (!_contentTypes.contains(contentType)) {
      errors.put("contentType.invalid", contentType);
      errorCount++;
    }
    return errorCount;
  }

  private int validateFilename(final String name, final Map<String, String> errors) {
    int errorCount = 0;
    if (!name.endsWith(".csv") && !name.endsWith(".xml")) {
      errors.put("suffix.invalid", name);
      errorCount++;
    }
    return errorCount;
  }
}
