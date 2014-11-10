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

    private static class FetchTailerListener extends TailerListenerAdapter {

        private StatusProvider statusProvider = null;
        private String statusKey = null;
        private String oldMessage = null;

        // 10 threads (2 waiting), 11 queues, 493 URLs queued, 5693 pages, 135
        // errors, 7.03 pages/s (3 last sec), 6011 kbits/s (18810 last sec)
        final Pattern p = Pattern.compile(".*queued, (\\d+) pages, (\\d+) errors, ([\\d\\.]+) pages/s.*");

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

}