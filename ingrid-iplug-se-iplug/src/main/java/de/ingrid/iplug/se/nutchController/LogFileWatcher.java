package de.ingrid.iplug.se.nutchController;

import java.io.File;

import org.apache.commons.io.input.Tailer;
import org.apache.commons.io.input.TailerListener;

/**
 * 
 * @author joachim
 * 
 */
public class LogFileWatcher {

    Tailer tailer = null;

    public LogFileWatcher(File file, TailerListener listener) {
        tailer = Tailer.create(file, listener, 1000, true);
    }

    public void close() {
        if (tailer != null) {
            tailer.stop();
        }
    }

}
