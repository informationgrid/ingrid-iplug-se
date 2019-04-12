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

package org.apache.nutch.analysis.de;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.AnalyzerWrapper;


/**
 * A simple German Analyzer that wraps the Lucene one.
 * @author joachim@wemove.com
 */
public class GermanAnalyzer extends AnalyzerWrapper {
    
    private final static Analyzer ANALYZER = 
            new org.apache.lucene.analysis.de.GermanAnalyzer();

    
    /** Creates a new instance of FrenchAnalyzer */
    public GermanAnalyzer() {
        super(Analyzer.GLOBAL_REUSE_STRATEGY);
    }


    @Override
    protected Analyzer getWrappedAnalyzer(String fieldName) {
        return ANALYZER;
    }


    @Override
    protected TokenStreamComponents wrapComponents(String fieldName, TokenStreamComponents components) {
        return components;
    }



}
