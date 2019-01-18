/*
 * **************************************************-
 * ingrid-iplug-se-nutch
 * ==================================================
 * Copyright (C) 2014 - 2019 wemove digital solutions GmbH
 * ==================================================
 * Licensed under the EUPL, Version 1.1 or – as soon they will be
 * approved by the European Commission - subsequent versions of the
 * EUPL (the "Licence");
 * 
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl5
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * **************************************************#
 */
/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.ingrid.iplug.se.nutch.scoring.webgraph;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.util.StringUtils;
import org.apache.hadoop.util.ToolRunner;
import org.apache.nutch.scoring.webgraph.LinkRank;
import org.apache.nutch.util.NutchConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Wraps the {@link LinkRank} class to catch the case when the weobgraph is
 * empty. In this case to not exit with an error code to make sure the crawl
 * process is not terminated.
 * 
 * @author joachim
 * 
 */
public class LinkRankWrapper extends LinkRank {

    public static final Logger LOG = LoggerFactory.getLogger(LinkRankWrapper.class);

    public static void main(String[] args) throws Exception {
        int res = ToolRunner.run(NutchConfiguration.create(), new LinkRankWrapper(), args);
        System.exit(res);
    }

    /**
     * Runs the LinkRankWrapper tool.
     */
    public int run(String[] args) throws Exception {

        Options options = new Options();
        OptionBuilder.withArgName("help");
        OptionBuilder.withDescription("show this help message");
        Option helpOpts = OptionBuilder.create("help");
        options.addOption(helpOpts);

        OptionBuilder.withArgName("webgraphdb");
        OptionBuilder.hasArg();
        OptionBuilder.withDescription("the web graph db to use");
        Option webgraphOpts = OptionBuilder.create("webgraphdb");
        options.addOption(webgraphOpts);

        CommandLineParser parser = new GnuParser();
        try {

            CommandLine line = parser.parse(options, args);
            if (line.hasOption("help") || !line.hasOption("webgraphdb")) {
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp("LinkRankWrapper", options);
                return -1;
            }

            String webGraphDb = line.getOptionValue("webgraphdb");

            analyze(new Path(webGraphDb));
            return 0;
        } catch (Exception e) {
            if (e.getMessage().equals("No links to process, is the webgraph empty?")) {
                LOG.info("No links to process, is the webgraph empty? Exit without error!");
                return 0;
            } else {
                LOG.error("LinkAnalysis: " + StringUtils.stringifyException(e));
                return -2;
            }
        }
    }
}
