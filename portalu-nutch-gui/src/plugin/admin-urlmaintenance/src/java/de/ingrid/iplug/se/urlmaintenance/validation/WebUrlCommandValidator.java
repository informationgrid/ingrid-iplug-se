package de.ingrid.iplug.se.urlmaintenance.validation;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;

import de.ingrid.iplug.se.urlmaintenance.commandObjects.ExcludeUrlCommand;
import de.ingrid.iplug.se.urlmaintenance.commandObjects.LimitUrlCommand;
import de.ingrid.iplug.se.urlmaintenance.commandObjects.StartUrlCommand;
import de.ingrid.iplug.se.urlmaintenance.persistence.dao.IStartUrlDao;
import de.ingrid.iplug.se.urlmaintenance.persistence.model.Metadata;
import de.ingrid.iplug.se.urlmaintenance.persistence.model.Provider;

@Service
public class WebUrlCommandValidator extends AbstractValidator<StartUrlCommand> {

    private final IStartUrlDao _urlDao;

    @Autowired
    public WebUrlCommandValidator(final IStartUrlDao urlDao) {
        _urlDao = urlDao;
    }

    public Errors validateStartUrl(final Errors errors) {
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
        return errors;
    }

    @SuppressWarnings("unchecked")
    public Errors validateLimitUrl(final Errors errors) {
        final List<LimitUrlCommand> commands = (List<LimitUrlCommand>) get(errors, "limitUrlCommands");
        final int index = commands.size() - 1;
        final LimitUrlCommand limitUrl = commands.get(index);
        
        final String url = limitUrl.getUrl();
        if (url == null || url.length() == 0) {
            errors.rejectValue("limitUrlCommands[" + index + "].url", getErrorKey(_typeClass, "limitUrl.url", IErrorKeys.EMPTY));
        } else {
            try {
                new URL(url);
            } catch (final MalformedURLException e) {
                errors.rejectValue("limitUrlCommands[" + index + "].url", getErrorKey(_typeClass, "limitUrl.url", IErrorKeys.MALFORMED));
            }
        }
        final Provider provider = limitUrl.getProvider();
        if (provider == null) {
            errors.rejectValue("limitUrlCommands[" + index + "].provider", getErrorKey(_typeClass, "limitUrl.provider", IErrorKeys.NULL));
        }

        final List<Metadata> metadatas = limitUrl.getMetadatas();
        boolean hasDatatype = false;
        boolean hasLang = false;
        for (final Metadata m : metadatas) {
            final String key = m.getMetadataKey();
            if (key.equals("datatype")) {
                hasDatatype = true;
            } else if (key.equals("lang")) {
                hasLang = true;
            }
        }
        if (!hasLang) {
            errors.rejectValue("limitUrlCommands[" + index + "].metadatas", getErrorKey(_typeClass,
                    "limitUrl.metadatas", "lang." + IErrorKeys.MISSING));
        }

        if (!hasDatatype) {
            errors.rejectValue("limitUrlCommands[" + index + "].metadatas", getErrorKey(_typeClass, "limitUrl.metadatas", "datatype." + IErrorKeys.MISSING));
        }

        return errors;
    }
    
    @SuppressWarnings("unchecked")
    public Errors validateExcludeUrl(final Errors errors) {
        final List<ExcludeUrlCommand> commands = (List<ExcludeUrlCommand>) get(errors, "excludeUrlCommands");
        final int index = commands.size() - 1;
        final ExcludeUrlCommand excludeUrl = commands.get(index);
        
        final String url = excludeUrl.getUrl();
        if (url == null || url.length() == 0) {
            errors.rejectValue("excludeUrlCommands[" + index + "].url", getErrorKey(_typeClass, "excludeUrl.url", IErrorKeys.EMPTY));
        } else {
            try {
                new URL(url);
            } catch (final MalformedURLException e) {
                errors.rejectValue("excludeUrlCommands[" + index + "].url", getErrorKey(_typeClass, "excludeUrl.url", IErrorKeys.MALFORMED));
            }
        }
        final Provider provider = excludeUrl.getProvider();
        if (provider == null) {
            errors.rejectValue("excludeUrlCommands[" + index + "].provider", getErrorKey(_typeClass, "excludeUrl.provider", IErrorKeys.NULL));
        }
        
        return errors;
    }
}
