package de.ingrid.iplug.se.urlmaintenance.validation;

import java.util.HashSet;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.web.multipart.MultipartFile;

import de.ingrid.iplug.se.urlmaintenance.importer.UploadCommand;

@Service
public class UploadCommandValidator extends AbstractValidator<UploadCommand> {

    private static final long MAX_SIZE = 3145728;

    private final Set<String> _contentTypes = new HashSet<String>();

    public UploadCommandValidator() {
        _contentTypes.add("application/xml");
        _contentTypes.add("application/text");
        _contentTypes.add("text/xml");
        _contentTypes.add("text/plain");
        _contentTypes.add(null);
    }

    public Errors validate(final Errors errors) {
        rejectIfEmptyOrWhitespace(errors, "type");

        final MultipartFile file = (MultipartFile) get(errors, "file");
        if (file.isEmpty()) {
            rejectError(errors, "file", IErrorKeys.NULL);
        } else {
            final String name = file.getOriginalFilename();
            if (!name.endsWith(".csv") && !name.endsWith(".xml")) {
                rejectError(errors, "file", IErrorKeys.INVALID);
            }

            final String contentType = file.getContentType();
            if (!_contentTypes.contains(contentType)) {
                rejectError(errors, "file", IErrorKeys.CONTENT);
            }

            final long size = file.getSize();
            if (size > MAX_SIZE) {
                rejectError(errors, "file", IErrorKeys.TO_LARGE);
            }
        }

        return errors;
    }
}
