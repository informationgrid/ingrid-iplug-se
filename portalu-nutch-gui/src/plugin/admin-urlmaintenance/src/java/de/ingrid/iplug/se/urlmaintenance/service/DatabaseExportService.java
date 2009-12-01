package de.ingrid.iplug.se.urlmaintenance.service;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.ingrid.iplug.se.urlmaintenance.DatabaseExport;
// TODO rwe: remove this class
@Service
public class DatabaseExportService {

  class ExportRunnable extends TimerTask {

    private final Log LOG = LogFactory.getLog(ExportRunnable.class);

    private final File _folder;

    public ExportRunnable(File folder) {
      _folder = folder;
    }

    @Override
    public void run() {
      try {
        LOG.info("export urls into " + _folder);
        _databaseExport.export("catalog", _folder);
        _databaseExport.export("web", _folder);
      } catch (IOException e) {
        LOG.error("can not export urls", e);
      }
    }

  }

  private static final long INTERVAL = TimeUnit.HOURS.toMillis(1L);

  private final DatabaseExport _databaseExport;

  @Autowired
  public DatabaseExportService(DatabaseExport databaseExport) {
    _databaseExport = databaseExport;
    start();
  }

  public void start() {
    String tmp = System.getProperty("java.io.tmpdir");
    File tmpFolder = new File(tmp, "portal-u-" + DatabaseExport.class.getName());
    Timer timer = new Timer("exportTimer", true);
    timer.schedule(new ExportRunnable(tmpFolder), new Date(), INTERVAL);
  }

}
