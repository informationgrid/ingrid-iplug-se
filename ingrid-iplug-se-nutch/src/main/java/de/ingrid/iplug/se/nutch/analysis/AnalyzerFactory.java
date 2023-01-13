/*
 * **************************************************-
 * ingrid-iplug-se-nutch
 * ==================================================
 * Copyright (C) 2014 - 2023 wemove digital solutions GmbH
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
package de.ingrid.iplug.se.nutch.analysis;

// Commons Logging imports

import org.apache.hadoop.conf.Configuration;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.nutch.plugin.Extension;
import org.apache.nutch.plugin.ExtensionPoint;
import org.apache.nutch.plugin.PluginRepository;
import org.apache.nutch.plugin.PluginRuntimeException;
import org.apache.nutch.util.ObjectCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


// Nutch imports

/**
 * Creates and caches {@link NutchAnalyzer} plugins.
 * 
 * @author J&eacute;r&ocirc;me Charron
 */
public class AnalyzerFactory {

    private final static String KEY = AnalyzerFactory.class.getName();

    public final static Logger LOG = LoggerFactory.getLogger(KEY);

    private final Analyzer DEFAULT_ANALYZER;

    private final ExtensionPoint extensionPoint;
    private final Configuration conf;

    public AnalyzerFactory(Configuration conf) {
        DEFAULT_ANALYZER = new StandardAnalyzer();
        this.conf = conf;
        this.extensionPoint = PluginRepository.get(conf).getExtensionPoint(NutchAnalyzer.X_POINT_ID);
        if (this.extensionPoint == null) {
            throw new RuntimeException("x point " + NutchAnalyzer.X_POINT_ID + " not found.");
        }
    }

    public static AnalyzerFactory get(Configuration conf) {
        ObjectCache objectCache = ObjectCache.get(conf);
        AnalyzerFactory factory = (AnalyzerFactory) objectCache.getObject(KEY);
        if (factory == null) {
            factory = new AnalyzerFactory(conf);
            objectCache.setObject(KEY, factory);
        }
        return factory;
    }

    /**
     * Returns the appropriate {@link Analyzer analyzer} implementation given a
     * language code.
     * 
     * <p>
     * Analyzer extensions should define the attribute "lang". The first plugin
     * found whose "lang" attribute equals the specified lang parameter is used.
     */
    public Analyzer get(String lang) {

        Analyzer analyzer = DEFAULT_ANALYZER;
        Extension extension = getExtension(lang);
        if (extension != null) {
            try {
                analyzer = (Analyzer) extension.getExtensionInstance();
            } catch (PluginRuntimeException pre) {
                LOG.warn("Could not get analyzer, proceed with default analyzer.");
            }
        }
        return analyzer;
    }

    private Extension getExtension(String lang) {
        ObjectCache objectCache = ObjectCache.get(conf);
        if (lang == null) {
            return null;
        }
        Extension extension = (Extension) objectCache.getObject(lang);
        if (extension == null) {
            extension = findExtension(lang);
            if (extension != null) {
                objectCache.setObject(lang, extension);
            }
        }
        return extension;
    }

    private Extension findExtension(String lang) {

        if (lang != null) {
            Extension[] extensions = this.extensionPoint.getExtensions();
            for (Extension extension : extensions) {
                if (lang.equals(extension.getAttribute("lang"))) {
                    return extension;
                }
            }
        }
        return null;
    }

    /**
     * Method used by unit test
     */
    protected Analyzer getDefault() {
        return DEFAULT_ANALYZER;
    }

}
