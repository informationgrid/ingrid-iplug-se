/**
 * Copyright 2005 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.ingrid.iplug.se;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.nutch.admin.ConfigurationUtil;
import org.apache.nutch.admin.GuiComponentDeployer;
import org.apache.nutch.admin.HttpServer;

import de.ingrid.iplug.se.crawl.sns.SnsRecordWriter;
import de.ingrid.utils.PlugDescription;
import de.ingrid.utils.xml.XMLSerializer;

/**
 * Starts the IPlug-SE administration application
 */
public class AdministrationApp {

    private static final String PARAM_WORKING_DIRECTORY = "workingDirectory";

    private static final String PARAM_COMMUNICATION = "descriptor";

    private static final String PARAM_PLUGDESCRIPTION = "plugdescription";

    private static final Log LOG = LogFactory.getLog(AdministrationApp.class);

    private static final int DEFAULT_PORT = 50060;

    private static PlugDescription loadPlugDescriptionFromFile(File plugDescriptionFile) throws IOException {
        LOG.info("load plugdescription: " + plugDescriptionFile.getAbsolutePath());
        InputStream resourceAsStream = new FileInputStream(plugDescriptionFile);
        XMLSerializer serializer = new XMLSerializer();
        PlugDescription plugDescription = (PlugDescription) serializer.deSerialize(resourceAsStream);
        plugDescription.setRecordLoader(true);
        return plugDescription;
    }

    @SuppressWarnings("static-access")
    public static void main(String[] args) throws Exception {

        CommandLineParser parser = new PosixParser();

        Options options = new Options();
        options.addOption(null, "port", true, "Starts the web-server on given port. (default: " + DEFAULT_PORT + ")");
        options.addOption(OptionBuilder.withLongOpt(PARAM_WORKING_DIRECTORY).withDescription(
                "Runns the indexer in PATH directory.").hasArg(true).withArgName("PATH").create());
        options.addOption(OptionBuilder.withLongOpt("secure").withDescription("Authenticate against IBus users.")
                .hasArg(false).create());
        options.addOption(OptionBuilder.withLongOpt(PARAM_PLUGDESCRIPTION).withDescription(
                "Uses PD as plugdescription.xml for this IPlug.").hasArg(true).withArgName("PD").create());
        options.addOption(OptionBuilder.withLongOpt(PARAM_COMMUNICATION).withDescription(
                "Uses COMMUNICATION as communication.xml for this IPlug.").hasArg(true).withArgName("COMMUNICATION")
                .create());

        try {
            // parse the command line arguments
            CommandLine line = parser.parse(options, args);

            File workingDirectory = null;
            int port = DEFAULT_PORT;

            if (line.hasOption("port")) {
                port = Integer.parseInt(line.getOptionValue("port"));
            }
            // When the plugdescription file is named, only the working
            // directory is read from this file.
            if (line.hasOption(PARAM_PLUGDESCRIPTION)) {
                System.setProperty(IKeys.PLUG_DESCRIPTION, line.getOptionValue(PARAM_PLUGDESCRIPTION));
                File plugDescriptionFile = new File(line.getOptionValue(PARAM_PLUGDESCRIPTION));
                if (plugDescriptionFile.exists()) {
                    PlugDescription plugDescription = loadPlugDescriptionFromFile(plugDescriptionFile);
                    workingDirectory = plugDescription.getWorkinDirectory();
                    LOG
                            .info("Using working directory '" + workingDirectory.getPath()
                                    + "' given from plugdescription file '"
                                    + line.getOptionValue(PARAM_PLUGDESCRIPTION) + "'.");
                } else {
                    LOG.warn("Named plugdescription file '" + plugDescriptionFile.getAbsolutePath()
                            + "' does not exists.");
                    workingDirectory = new File("/tmp/nutchGui");
                    LOG.warn("Using temporary working directory '" + workingDirectory.getPath()
                            + "' to be able to run the configuration tool");
                }
            }
            // When the working directory is named explicitly, we will use this
            if (line.hasOption(PARAM_WORKING_DIRECTORY)) {
                workingDirectory = new File(line.getOptionValue(PARAM_WORKING_DIRECTORY));
                LOG.info("Using working directory '" + workingDirectory.getPath() + "' provided by programm argument.");
            }
            if (workingDirectory == null) {
                // The working directory is not provided.
                new HelpFormatter().printHelp("AdministrationApp", options);
                throw new IllegalStateException(
                        "To run the Indexer a working directory must be provided in a plugindescription.xml file or by a Program argument.");
            }
            if (line.hasOption(PARAM_COMMUNICATION)) {
                System.setProperty(IKeys.COMMUNICATION, line.getOptionValue(PARAM_COMMUNICATION));
            }
            boolean secure = line.hasOption("secure");
            HttpServer httpServer = new HttpServer(port, secure);
            httpServer.startHttpServer();

            ConfigurationUtil configurationUtil = new ConfigurationUtil(workingDirectory);
            if (!configurationUtil.existsConfiguration("general")) {
                configurationUtil.createNewConfiguration("general");
            }

            httpServer.addContextAttribute("configurationUtil", configurationUtil);

            GuiComponentDeployer componentDeployer = new GuiComponentDeployer(httpServer, configurationUtil,
                    workingDirectory);
            componentDeployer.start();
        } catch (ParseException exp) {
            exp.printStackTrace();
            new HelpFormatter().printHelp("AdministrationApp", options);
        }
    }
}
