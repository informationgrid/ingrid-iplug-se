package de.ingrid.iplug.se.webapp.validation;

import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;

import de.ingrid.admin.validation.AbstractValidator;
import de.ingrid.iplug.se.DatabaseConnection;

/**
 * Validator for database connection dialog.
 * 
 * 
 * @author joachim@wemove.com
 *
 */
@Service
public class DatabaseConnectionValidator extends AbstractValidator<DatabaseConnection> {

    public final Errors validateDBParams(final BindingResult errors) {
        rejectIfEmptyOrWhitespace(errors, "dataBaseDriver");
        rejectIfEmptyOrWhitespace(errors, "connectionURL");
        rejectIfEmptyOrWhitespace(errors, "user");
        return errors;
    }
}
