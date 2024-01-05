/*
 * **************************************************-
 * ingrid-iplug-se-iplug
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
 * 
 */
package de.ingrid.iplug.se.nutchController;

import de.ingrid.utils.statusprovider.StatusProvider;
import org.apache.commons.io.input.TailerListenerAdapter;

import java.io.File;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
        private String statusKey;
        private int totalQueued;
        private String oldMessage = null;
        private Date startTime = null;
        // 2022-12-03 15:03:56,173 INFO o.a.n.f.QueueFeeder [QueueFeeder] 	67	SUCCESSFULLY_QUEUED
        final Pattern pTotalQueued = Pattern.compile(".*\\s(\\d+)\\s+SUCCESSFULLY_QUEUED.*");

        // 2022-12-03 15:03:58,356 INFO o.a.n.f.Fetcher [LocalJobRunner Map Task Executor #0] -activeThreads=10, spinWaiting=10, fetchQueues.totalSize=66, fetchQueues.getQueueCount=1
        final Pattern pQueued = Pattern.compile(".*fetchQueues.totalSize=(\\d+),.*");

        public FetchTailerListener(StatusProvider statusProvider, String statusKey) {
            this.statusProvider = statusProvider;
            this.statusKey = statusKey;
            this.oldMessage = statusProvider.getStateMsg(statusKey);
        }

        public void handle(String line) {
            final Matcher mTotalQueued = pTotalQueued.matcher(line);
            if (mTotalQueued.matches()) {
                this.totalQueued = Integer.parseInt(mTotalQueued.group(1));
                this.startTime = new Date();
            }
            final Matcher mQueued = pQueued.matcher(line);
            if (mQueued.matches()) {
                StringBuilder msg = new StringBuilder();
                int pQueued = Integer.parseInt(mQueued.group(1));
                int pFetched = (this.totalQueued - pQueued);
                if (pFetched > this.totalQueued)  {
                    pFetched = this.totalQueued;
                }
                float pps = (float) pFetched / ( (new Date().getTime() - this.startTime.getTime() ) / 1000);
                msg.append(" ( ");
                msg.append(pFetched);
                msg.append(" / ");
                msg.append(totalQueued);
                msg.append(" pages fetched. " + pps + " pages/sec. )");
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

        // 2022-12-03 16:10:23,483 INFO o.a.n.s.w.LinkRank [main] Analysis: Starting iteration 10 of 10
        final Pattern p = Pattern.compile(".*LinkRank.*Analysis: Starting iteration (\\d+) of (\\d+).*");

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
