package de.ingrid.admin.validation;

import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;

import de.ingrid.admin.command.CommunicationCommandObject;

@Service
public class CommunicationValidator extends AbstractValidator<CommunicationCommandObject> {

    public final Errors validateProxyUrl(final Errors errors, final String defaultUrl) {
        rejectIfEmptyOrWhitespace(errors, "proxyServiceUrl");
        if (defaultUrl.equals(get(errors, "proxyServiceUrl"))) {
            rejectError(errors, "proxyServiceUrl", IErrorKeys.INVALID);
        }
        return errors;
    }

    public final Errors validateBus(final Errors errors) {
        rejectIfEmptyOrWhitespace(errors, "busProxyServiceUrl");
        rejectIfEmptyOrWhitespace(errors, "ip");
        rejectIfEmptyOrWhitespace(errors, "port");

        final Integer port = getInteger(errors, "port");
        if (null != port && port < 0) {
            rejectError(errors, "port", IErrorKeys.INVALID);
        }

        return errors;
    }
}
