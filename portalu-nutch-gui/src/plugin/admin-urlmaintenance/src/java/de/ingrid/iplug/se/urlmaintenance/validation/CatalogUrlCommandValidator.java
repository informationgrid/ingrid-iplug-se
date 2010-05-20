package de.ingrid.iplug.se.urlmaintenance.validation;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;

import de.ingrid.iplug.se.urlmaintenance.commandObjects.CatalogUrlCommand;
import de.ingrid.iplug.se.urlmaintenance.persistence.dao.ICatalogUrlDao;
import de.ingrid.iplug.se.urlmaintenance.persistence.model.Metadata;
import de.ingrid.iplug.se.urlmaintenance.persistence.model.Provider;

@Service
public class CatalogUrlCommandValidator extends AbstractValidator<CatalogUrlCommand> {

    private final ICatalogUrlDao _urlDao;

    @Autowired
    public CatalogUrlCommandValidator(final ICatalogUrlDao urlDao) {
        _urlDao = urlDao;
    }

    @SuppressWarnings("unchecked")
    public Errors validate(final Errors errors) {
        final String url = (String) get(errors, "url");
        rejectIfEmptyOrWhitespace(errors, "url");
        if (url != null && url.length() > 0) {
            try {
                new URL(url);
            } catch (final MalformedURLException e) {
                rejectError(errors, "url", IErrorKeys.MALFORMED);
            }
        }

        final Provider provider = (Provider) get(errors, "provider");
        if (provider == null) {
            rejectError(errors, "provider", IErrorKeys.NULL);
        } else {
            final Long id = (Long) get(errors, "id");
            if ((id == null || id < 0) && _urlDao.getByUrl(url, provider.getId()).size() > 0) {
                rejectError(errors, "url", IErrorKeys.DUPLICATE);
            }
        }

        final List<Metadata> metadatas = (List<Metadata>) get(errors, "metadatas");

        String datatype = null;
        boolean hasDefault = false;
        boolean hasLang = false;
        boolean hasTopic = false;
        boolean hasFunct = false;
        boolean hasRubric = false;
        for (final Metadata m : metadatas) {
            final String key = m.getMetadataKey();
            if (key.equals("datatype")) {
                if (m.getMetadataValue().equals("default")) {
                    hasDefault = true;
                } else {
                    datatype = m.getMetadataValue();
                }
            } else if (key.equals("topic")) {
                hasTopic = true;
            } else if (key.equals("funct_category")) {
                hasFunct = true;
            } else if (key.equals("lang")) {
                hasLang = true;
            } else if (key.equals("service") || key.equals("measure")) {
                hasRubric = true;
            }
        }

        if (!hasLang) {
            rejectError(errors, "metadatas", "lang." + IErrorKeys.MISSING);
        }

        if (!hasDefault) {
            rejectError(errors, "metadatas", "default." + IErrorKeys.MISSING);
        }

        if (datatype == null) {
            rejectError(errors, "metadatas", "datatype." + IErrorKeys.NULL);
        } else if (datatype.equals("topics")) {
            if (!hasTopic) {
                rejectError(errors, "metadatas", "topic." + IErrorKeys.MISSING);
            }
            if (!hasFunct) {
                rejectError(errors, "metadatas", "funct." + IErrorKeys.MISSING);
            }
        } else if (datatype.equals("service") || datatype.equals("measure")) {
            if (!hasRubric) {
                rejectError(errors, "metadatas", "rubric." + IErrorKeys.MISSING);
            }
        } else {
            rejectError(errors, "metadatas", "datatype." + IErrorKeys.INVALID);
        }

        return errors;
    }
}
