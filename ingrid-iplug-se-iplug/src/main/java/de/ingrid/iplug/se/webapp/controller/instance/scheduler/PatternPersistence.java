/*
 * **************************************************-
 * ingrid-iplug-se-iplug
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

import java.io.IOException;

import org.springframework.stereotype.Service;

@Service
public class PatternPersistence extends Persistence<Pattern> {

    public PatternPersistence() {}

    public void savePattern(String pattern, String instanceName) throws IOException {
        Pattern patternObject = new Pattern();
        patternObject.setPattern( pattern );
        makePersistent( patternObject, instanceName );
//        Scheduler scheduler = _scheduler.getScheduler( instanceName );
//        if (!scheduler.isStarted()) {
//            _scheduler.schedule( pattern );
//        } else {
//            _scheduler.reschedule( instanceName, pattern );
//        }

    }

    public Pattern loadPattern(String instanceName) throws Exception {
        return load( Pattern.class, instanceName );
    }

    public boolean existsPattern(String instanceName) {
        return exists( Pattern.class, instanceName );
    }

    public void deletePattern(String instanceName) throws IOException {
        makeTransient( Pattern.class, instanceName );
//        _scheduler.deschedule( _scheduleId );
//        _scheduleId = null;
    }

    // public void setWorkingDirectory(File workingDirectory) throws Exception {
    // super.setWorkingDirectory(workingDirectory);
    // if (existsPattern()) {
    // Pattern pattern = loadPattern();
    // _scheduleId = _scheduler.schedule(pattern.getPattern(), _runnable);
    // }
    // }
}
