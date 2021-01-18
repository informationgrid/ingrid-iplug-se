/*
 * **************************************************-
 * ingrid-iplug-se-iplug
 * ==================================================
 * Copyright (C) 2014 - 2021 wemove digital solutions GmbH
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

public class ClockCommand extends CrawlCommand {

    public static enum Period {
        AM, PM
    }

    private String _time;
    
    private Integer _hour;

    private Integer _minute;

    private Period _period;

    public Integer getHour() {
        return _hour;
    }

    public String getTime() {
        return _time;
    }

    public void setTime(String time) {
        if (time.isEmpty()) {
            this._hour = this._minute = null;
            return;
        }
        this._time = time;
        String[] timeSplitted = this._time.split( " " );
        String[] hourMinutes = timeSplitted[0].split( ":" );
        this._hour = Integer.valueOf( hourMinutes[0] );
        this._minute = Integer.valueOf( hourMinutes[1] );
        this._period = "PM".equals( timeSplitted[1] ) ? Period.PM : Period.AM;
    }

    public Integer getMinute() {
        return _minute;
    }

    public Period getPeriod() {
        return _period;
    }


}
