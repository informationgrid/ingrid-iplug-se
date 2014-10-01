package de.ingrid.iplug.se.utils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.file.Path;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

import de.ingrid.iplug.se.conf.UrlMaintenanceSettings;
import de.ingrid.iplug.se.conf.UrlMaintenanceSettings.MetaElement;

/**
 * Tool for accessing the instance configuration.
 * 
 * @author joachim
 * 
 */
public class InstanceConfigurationTool {

    private final static Log log = LogFactory.getLog(InstanceConfigurationTool.class);

    UrlMaintenanceSettings settings = null;

    /**
     * Opens an existing instance configuration.
     * 
     * @param confPath
     */
    public InstanceConfigurationTool(Path confPath) {

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        InputStream reader = null;
        try {
            reader = new FileInputStream(confPath.toAbsolutePath().toFile());
            settings = gson.fromJson(new InputStreamReader(reader, "UTF-8"), UrlMaintenanceSettings.class);
        } catch (FileNotFoundException | JsonSyntaxException | JsonIOException | UnsupportedEncodingException e) {
            log.error("Error opening instance configuration: " + confPath, e);
            throw new RuntimeException("Error opening nutch configuration: " + confPath, e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    log.error("Error closing instance configuration: " + confPath, e);
                    throw new RuntimeException("Error closing instance configuration: " + confPath, e);
                }
            }
        }

    }

    public UrlMaintenanceSettings getSettings() {
        return settings;
    }

    public List<MetaElement> getMetadata() {
        return settings.getMetadata();
    }

}
