/*
 * **************************************************-
 * ingrid-iplug-se-nutch
 * ==================================================
 * Copyright (C) 2014 - 2024 wemove digital solutions GmbH
 * ==================================================
 * Licensed under the EUPL, Version 1.2 or – as soon they will be
 * approved by the European Commission - subsequent versions of the
 * EUPL (the "Licence");
 * 
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * **************************************************#
 */
/**
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

package de.ingrid.iplug.se.nutch.analysis;

import crawlercommons.robots.BaseRobotRules;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.apache.nutch.crawl.CrawlDatum;
import org.apache.nutch.crawl.SignatureFactory;
import org.apache.nutch.parse.Parse;
import org.apache.nutch.parse.ParseResult;
import org.apache.nutch.parse.ParseSegment;
import org.apache.nutch.parse.ParseUtil;
import org.apache.nutch.protocol.Content;
import org.apache.nutch.protocol.Protocol;
import org.apache.nutch.protocol.ProtocolFactory;
import org.apache.nutch.protocol.ProtocolOutput;
import org.apache.nutch.scoring.ScoringFilters;
import org.apache.nutch.util.NutchConfiguration;
import org.apache.nutch.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Urls tester, useful for testing urls on crawlability.
 * 
 * @author joachim@wemove.com
 */

public class UrlTester extends Configured implements Tool {

    private Configuration conf;

    public UrlTester() {
    }

    public int run(String[] args) throws Exception {
        boolean dumpText = false;
        String contentType = null;
        String url = null;

        String usage = "Usage: UrlTester url";

        if (args.length == 0) {
            System.out.println(usage);
            return (-1);
        }

        for (int i = 0; i < args.length; i++) {
            if (i != args.length - 1) {
                System.out.println(usage);
                System.exit(-1);
            } else {
                url = args[i];
            }
        }

        System.out.println("Test url: " + url);

        CrawlDatum cd = new CrawlDatum();
        ProtocolFactory factory = new ProtocolFactory(conf);
        Protocol protocol = factory.getProtocol(url);
        Text turl = new Text(url);
        List<Content> robotsTxtContent = new ArrayList<Content>();
        BaseRobotRules rules = protocol.getRobotRules(turl, cd, robotsTxtContent);
        long maxCrawlDelay = conf.getInt("fetcher.max.crawl.delay", 30) * 1000;
        if (!rules.isAllowed(turl.toString())) {
            System.out.println("Denied by robots.txt.");
            System.out.println("");
            return (0);
        } else if (rules.getCrawlDelay() > 0) {
            if (rules.getCrawlDelay() > maxCrawlDelay && maxCrawlDelay >= 0) {
                System.out.println("Crawl-Delay for too long (" + rules.getCrawlDelay() + ").");
                System.out.println("");
                return (0);
            } else {
                System.out.println("Crawl-Delay: " + rules.getCrawlDelay() + " [ms]");
            }
        }

        ProtocolOutput output = protocol.getProtocolOutput(turl, cd);

        if (!output.getStatus().isSuccess()) {
            System.out.println("Fetch failed with protocol status: " + output.getStatus());
            System.out.println("");
            return (0);
        }

        Content content = output.getContent();

        if (content == null) {
            System.out.println("No content for " + url);
            System.out.println("");
            return (0);
        }

        contentType = content.getContentType();

        if (contentType == null) {
            System.out.println("Failed to determine content type!");
            System.out.println("");
            return (0);
        }

        if (ParseSegment.isTruncated(content)) {
            System.out.println("Content is truncated, parse may fail!");
        }

        ScoringFilters scfilters = new ScoringFilters(conf);
        // call the scoring filters
        try {
            scfilters.passScoreBeforeParsing(turl, cd, content);
        } catch (Exception e) {
            System.out.println("Couldn't pass score, url " + turl.toString() + " (" + e + ")");
        }

        ParseResult parseResult = new ParseUtil(conf).parse(content);

        if (parseResult == null) {
            System.out.println("Problem with parse.");
            System.out.println("");
            return 0;
        }

        // Calculate the signature
        byte[] signature = SignatureFactory.getSignature(getConf()).calculate(content, parseResult.get(new Text(url)));

        System.out.println("contentType: " + contentType);
        System.out.println("signature: " + StringUtil.toHexString(signature));

        // call the scoring filters
        try {
            scfilters.passScoreAfterParsing(turl, content, parseResult.get(turl));
        } catch (Exception e) {
            System.out.println("Couldn't pass score, url " + turl + " (" + e + ")");
        }

        for (java.util.Map.Entry<Text, Parse> entry : parseResult) {
            Parse parse = entry.getValue();
            System.out.println("\n---------\nParseData\n---------\n");
            System.out.print(parse.getData().toString());
            if (dumpText) {
                System.out.println("---------\nParseText\n---------\n");
                System.out.print(parse.getText());
            }
        }
        System.out.println("");

        return 0;
    }

    @Override
    public Configuration getConf() {
        return conf;
    }

    @Override
    public void setConf(Configuration c) {
        conf = c;
    }

    public static void main(String[] args) throws Exception {
        int res = ToolRunner.run(NutchConfiguration.create(), new UrlTester(), args);
        System.exit(res);
    }

}
