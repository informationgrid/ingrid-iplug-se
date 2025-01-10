/*
 * **************************************************-
 * ingrid-iplug-se-iplug
 * ==================================================
 * Copyright (C) 2014 - 2025 wemove digital solutions GmbH
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
package de.ingrid.iplug.se.webapp.controller.instance.scheduler;

import java.io.File;
import java.io.IOException;

import de.ingrid.iplug.se.Configuration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CrawlDataPersistence extends Persistence<CrawlData> {

    @Autowired
    private Configuration seConfig;

    public void saveCrawlData(Integer depth, Integer topn, String instanceName) throws IOException {
        CrawlData crawlData = new CrawlData();
        crawlData.setDepth( depth );
        crawlData.setTopn( topn );
        crawlData.setWorkingDirectory( new File( seConfig.getInstancesDir(), instanceName ) );
        makePersistent( crawlData, instanceName );
    }

    public CrawlData loadCrawlData(String instanceName) throws IOException, ClassNotFoundException {
        return load( CrawlData.class, instanceName );
    }

    public boolean existsCrawlData(String instanceName) {
        return exists( CrawlData.class, instanceName );
    }

    public void deleteCrawlData(String instanceName) throws IOException {
        makeTransient( CrawlData.class, instanceName );
    }

}
