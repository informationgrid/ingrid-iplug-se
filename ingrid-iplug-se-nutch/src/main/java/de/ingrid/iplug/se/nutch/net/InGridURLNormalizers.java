/*
 * **************************************************-
 * ingrid-iplug-se-nutch
 * ==================================================
 * Copyright (C) 2014 - 2018 wemove digital solutions GmbH
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

package de.ingrid.iplug.se.nutch.net;


/**
 * This class uses a "chained filter" pattern to run defined normalizers.
 * Different lists of normalizers may be defined for different "scopes", or
 * contexts where they are used (note however that they need to be activated
 * first through <tt>plugin.include</tt> property).
 * 
 * <p>There is one global scope defined by default, which consists of all
 * active normalizers. The order in which these normalizers
 * are executed may be defined in "urlnormalizer.order" property, which lists
 * space-separated implementation classes (if this property is missing normalizers
 * will be run in random order). If there are more
 * normalizers activated than explicitly named on this list, the remaining ones
 * will be run in random order after the ones specified on the list are executed.</p>
 * <p>You can define a set of contexts (or scopes) in which normalizers may be
 * called. Each scope can have its own list of normalizers (defined in
 * "urlnormalizer.scope.<scope_name>" property) and its own order (defined in
 * "urlnormalizer.order.<scope_name>" property). If any of these properties are
 * missing, default settings are used for the global scope.</p>
 * <p>In case no normalizers are required for any given scope, a
 * <code>de.ingrid.iplug.se.nutch.net.urlnormalizer.pass.PassURLNormalizer</code> should be used.</p>
 * <p>Each normalizer may further select among many configurations, depending on
 * the scope in which it is called, because the scope name is passed as a parameter
 * to each normalizer. You can also use the same normalizer for many scopes.</p>
 * <p>Several scopes have been defined, and various Nutch tools will attempt using
 * scope-specific normalizers first (and fall back to default config if scope-specific
 * configuration is missing).</p>
 * <p>Normalizers may be run several times, to ensure that modifications introduced
 * by normalizers at the end of the list can be further reduced by normalizers
 * executed at the beginning. By default this loop is executed just once - if you want
 * to ensure that all possible combinations have been applied you may want to run
 * this loop up to the number of activated normalizers. This loop count can be configured
 * through <tt>urlnormalizer.loop.count</tt> property. As soon as the url is
 * unchanged the loop will stop and return the result.</p>
 * 
 * @author Andrzej Bialecki
 */
public final class InGridURLNormalizers {
  

  /** Scope used when updating the extracting outlinks from parsed data and update the CrawlDb based on BW db. */
  public static final String SCOPE_BWDB = "bwdb";
}
