package de.ingrid.iplug.se.urlmaintenance.validation;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;

import de.ingrid.iplug.se.urlmaintenance.commandObjects.CatalogUrlCommand;
import de.ingrid.iplug.se.urlmaintenance.persistence.dao.ICatalogUrlDao;
import de.ingrid.iplug.se.urlmaintenance.persistence.model.CatalogUrl;
import de.ingrid.iplug.se.urlmaintenance.persistence.model.Metadata;
import de.ingrid.iplug.se.urlmaintenance.persistence.model.Provider;

@Service
public class CatalogUrlCommandValidator extends AbstractValidator<CatalogUrlCommand> {

    private final ICatalogUrlDao _urlDao;
    private String _datatype = "";

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
            // check if url was already defined within the same datatype
            // (topics, service or measure)
            if (id == null || id < 0) {
                List<CatalogUrl> catalogUrls = _urlDao.getByUrl(url, provider.getId());
                for (CatalogUrl catalogUrl : catalogUrls) {
                    List<Metadata> metadatas = catalogUrl.getMetadatas();
                    for (Metadata metadata : metadatas) {
                        if (metadata.getMetadataKey().equals("datatype")
                                && metadata.getMetadataValue().equals(_datatype)) {
                            rejectError(errors, "url", IErrorKeys.DUPLICATE);
                        }
                    }
                }
            }
        }

        final List<Metadata> metadatas = (List<Metadata>) get(errors, "metadatas");

        String datatype = null;
        boolean hasDefault = false;
        boolean hasLang = false;
        boolean hasTopic = false;
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
        } else if (datatype.equals("service") || datatype.equals("measure")) {
            if (!hasRubric) {
                rejectError(errors, "metadatas", "rubric." + IErrorKeys.MISSING);
            }
        } else {
            rejectError(errors, "metadatas", "datatype." + IErrorKeys.INVALID);
        }

        return errors;
    }
    
    /**
     * This method sets the datatype to identify the type of the catalog.
     * It's mainly used to check if an URL already exists in a catalog of
     * a certain type, which can be topics, service, measure.
     * 
     * @param datatype
     */
    public void setUsedDatatype(String datatype) {
        _datatype = datatype;
    }
}
