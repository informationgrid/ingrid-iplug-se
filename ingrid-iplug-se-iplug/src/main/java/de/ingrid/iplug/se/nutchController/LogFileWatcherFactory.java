/*
 * **************************************************-
 * ingrid-iplug-se-iplug
 * ==================================================
 * Copyright (C) 2014 - 2015 wemove digital solutions GmbH
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
 * 
 */
package de.ingrid.iplug.se.nutchController;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.input.TailerListenerAdapter;

/**
 * 
 * @author joachim
 * 
 */
public class LogFileWatcherFactory {

    public static LogFileWatcher getFetchLogfileWatcher(File file, StatusProvider statusProvider, String statusKey) {
        return new LogFileWatcher(file, new LogFileWatcherFactory.FetchTailerListener(statusProvider, statusKey));
    }

    public static LogFileWatcher getDeduplicationLogfileWatcher(File file, StatusProvider statusProvider, String statusKey) {
        return new LogFileWatcher(file, new LogFileWatcherFactory.DeduplicationTailerListener(statusProvider, statusKey));
    }

    public static LogFileWatcher getCleaningJobLogfileWatcher(File file, StatusProvider statusProvider, String statusKey) {
        return new LogFileWatcher(file, new LogFileWatcherFactory.CleaningJobTailerListener(statusProvider, statusKey));
    }

    public static LogFileWatcher getWepgraphLogfileWatcher(File file, StatusProvider statusProvider, String statusKey) {
        return new LogFileWatcher(file, new LogFileWatcherFactory.WebgraphTailerListener(statusProvider, statusKey));
    }
    
    
    private static class FetchTailerListener extends TailerListenerAdapter {

        private StatusProvider statusProvider = null;
        private String statusKey = null;
        private String oldMessage = null;

        // 10 threads (2 waiting), 11 queues, 493 URLs queued, 5693 pages, 135
        // errors, 7.03 pages/s (3 last sec), 6011 kbits/s (18810 last sec)
        final Pattern p = Pattern.compile(".*queued, (\\d+) pages, (\\d+) errors, ([\\d\\.,]+) pages/s.*");

        public FetchTailerListener(StatusProvider statusProvider, String statusKey) {
            this.statusProvider = statusProvider;
            this.statusKey = statusKey;
            this.oldMessage = statusProvider.getStateMsg(statusKey);
        }

        public void handle(String line) {
            final Matcher m = p.matcher(line);
            if (m.matches()) {
                StringBuilder msg = new StringBuilder();
                msg.append(" ( ");
                msg.append(m.group(1));
                msg.append(" pages, ");
                msg.append(m.group(2));
                msg.append(" errors, ");
                msg.append(m.group(3));
                msg.append(" pages/s )");
                statusProvider.addState(statusKey, oldMessage.concat(msg.toString()));
            }
        }
    }
    
    private static class DeduplicationTailerListener extends TailerListenerAdapter {

        private StatusProvider statusProvider = null;
        private String statusKey = null;
        private String oldMessage = null;

        // Deduplication: 0 documents marked as duplicates
        final Pattern p = Pattern.compile(".*Deduplication: (\\d+) documents marked as duplicates.*");

        public DeduplicationTailerListener(StatusProvider statusProvider, String statusKey) {
            this.statusProvider = statusProvider;
            this.statusKey = statusKey;
            this.oldMessage = statusProvider.getStateMsg(statusKey);
        }

        public void handle(String line) {
            final Matcher m = p.matcher(line);
            if (m.matches()) {
                StringBuilder msg = new StringBuilder();
                msg.append(" ( new duplicate documents: ");
                msg.append(m.group(1));
                msg.append(" )");
                statusProvider.addState(statusKey, oldMessage.concat(msg.toString()));
            }
        }
    }
    
    private static class CleaningJobTailerListener extends TailerListenerAdapter {

        private StatusProvider statusProvider = null;
        private String statusKey = null;
        private String oldMessage = null;

        // indexer.CleaningJob - CleaningJob: deleted a total of 0 documents
        final Pattern p = Pattern.compile(".*indexer.CleaningJob - CleaningJob: deleted a total of (\\d+) documents.*");

        public CleaningJobTailerListener(StatusProvider statusProvider, String statusKey) {
            this.statusProvider = statusProvider;
            this.statusKey = statusKey;
            this.oldMessage = statusProvider.getStateMsg(statusKey);
        }

        public void handle(String line) {
            final Matcher m = p.matcher(line);
            if (m.matches()) {
                StringBuilder msg = new StringBuilder();
                msg.append(" ( deleted documents: ");
                msg.append(m.group(1));
                msg.append(" )");
                statusProvider.addState(statusKey, oldMessage.concat(msg.toString()));
            }
        }
    }    
    
    private static class WebgraphTailerListener extends TailerListenerAdapter {

        private StatusProvider statusProvider = null;
        private String statusKey = null;
        private String oldMessage = null;

        // webgraph.LinkRank - Analysis: Starting iteration 10 of 10
        final Pattern p = Pattern.compile(".*webgraph\\.LinkRank - Analysis: Starting iteration (\\d+) of (\\d+).*");

        public WebgraphTailerListener(StatusProvider statusProvider, String statusKey) {
            this.statusProvider = statusProvider;
            this.statusKey = statusKey;
            this.oldMessage = statusProvider.getStateMsg(statusKey);
        }

        public void handle(String line) {
            final Matcher m = p.matcher(line);
            if (m.matches()) {
                StringBuilder msg = new StringBuilder();
                msg.append(" ( analysis iteration: ");
                msg.append(m.group(1));
                msg.append("/");
                msg.append(m.group(2));
                msg.append(" )");
                statusProvider.addState(statusKey, oldMessage.concat(msg.toString()));
            }
        }
    }

}
