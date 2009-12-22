package de.ingrid.admin;

import java.beans.PropertyEditorSupport;
import java.io.File;

public class WorkingDirEditor extends PropertyEditorSupport {

    @Override
    public void setAsText(final String text) throws IllegalArgumentException {
        if (null != text && !"".equals(text)) {
            setValue(new File(text));
        }
    }

    @Override
    public String getAsText() {
        final Object value = getValue();
        return value != null ? value.toString() : "";
    }

}
