package de.ingrid.admin.command;

import java.io.File;

import de.ingrid.admin.StringUtils;
import de.ingrid.iplug.se.IKeys;
import de.ingrid.utils.PlugDescription;
import de.ingrid.utils.xml.XMLSerializer;

public class PlugdescriptionCommandObject extends PlugDescription {

    // needed for xmlserializer
    public PlugdescriptionCommandObject() {

    }

    @SuppressWarnings("unchecked")
    public PlugdescriptionCommandObject(final File file) throws Exception {
        if (file.exists()) {
            final XMLSerializer serializer = new XMLSerializer();
            serializer.aliasClass(PlugDescription.class.getName(), PlugDescription.class);
            putAll((PlugDescription) serializer.deSerialize(file));
        }
    }

    public String getTest() {
        return "test";
    }

    public void setDataTypes(final String... types) {
        remove(DATA_TYPE);
        for (final String type : types) {
            addDataType(type);
        }
    }

    public void removePartner(final String partner) {
        removeFromList(PlugDescription.PARTNER, partner);
    }

    public void removeProvider(final String provider) {
        removeFromList(PlugDescription.PROVIDER, provider);
    }

    @Override
    public void addDataType(final String type) {
        if (!StringUtils.isEmptyOrWhiteSpace(type) && !existsInArray(getDataTypes(), type)) {
            super.addDataType(type);
        }
    }

    @Override
    public void addPartner(final String partner) {
        if (!StringUtils.isEmptyOrWhiteSpace(partner) && !existsInArray(getPartners(), partner)) {
            super.addPartner(partner);
        }
    }

    @Override
    public void addProvider(final String provider) {
        if (!StringUtils.isEmptyOrWhiteSpace(provider) && !existsInArray(getProviders(), provider)) {
            super.addProvider(provider);
        }
    }

    @Override
    public void setOrganisationAbbr(final String provider) {
        super.setOrganisationAbbr(provider);
        addProvider(provider);
    }

    @Override
    public void setOrganisationPartnerAbbr(final String partner) {
        super.setOrganisationPartnerAbbr(partner);
        addPartner(partner);
    }

    @Override
    public int getIplugAdminGuiPort() {
        if (containsKey(PlugDescription.IPLUG_ADMIN_GUI_PORT)) {
            return super.getIplugAdminGuiPort();
        }
        return getOriginalPort();
    }
    
    public int getOriginalPort() {
        return Integer.parseInt(System.getProperty(IKeys.PORT));
    }
    
    public String getOriginalWorkingDir() {
        return System.getProperty(IKeys.WORKING_DIR);
    }

    @Override
    public void setWorkinDirectory(final File workinDirectory) {
        if (workinDirectory != null) {
            super.setWorkinDirectory(workinDirectory);
        }
    }

    private boolean existsInArray(final String[] things, final String thing) {
        for (final String t : things) {
            if (t.equals(thing)) {
                return true;
            }
        }
        return false;
    }
}
