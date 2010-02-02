package org.apache.nutch.mail;

import java.io.File;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.nutch.tools.UrlReporter;
import org.apache.nutch.util.ZipUtil;

public class MailService {

    public static final String TEMP_DIR = "nutch_reports";

    protected static Log LOG = LogFactory.getLog(MailService.class);

    protected static MailService _instance;

    private final MailSender _mailSender;

    private MailService(final Configuration configuration) {
        _mailSender = MailSender.get(configuration);
    }

    public static MailService get(final Configuration configuration) {
        if (_instance == null) {
            _instance = new MailService(configuration);
        }
        return _instance;
    }

    public void sendSegmentReport(final FileSystem fileSystem, final Path segment, final Integer currentDepth)
            throws IOException {
        LOG.info("sending report mail for segment '" + segment + "'");
        // create file name
        final Path report = new Path(segment, UrlReporter.REPORT);
        final String segmentName = segment.getName();
        final Path crawlPath = segment.getParent().getParent();
        final String crawlName = crawlPath.getName();
        final Path reportPath = new Path(crawlPath, TEMP_DIR + "/" + segmentName + "_" + currentDepth);

        // create temp local file report
        LOG.debug("moving report file to local file");
        fileSystem.copyToLocalFile(report, reportPath);
        final File reportFile = new File(reportPath.toString());

        // load attachment
        LOG.debug("creating attachment");
        File attachment = reportFile;
        if (reportFile.exists()) {
            // compress dir
            if (reportFile.isDirectory()) {
                attachment = ZipUtil.zip(reportFile, reportFile);
            }

            // create message
            final String subject = "URL report for crawl '" + crawlName + "' of segment '" + segmentName + "'";
            final StringBuilder sb = new StringBuilder();
            sb.append("crawl: " + crawlName + "\n");
            sb.append("segment: " + segmentName + "\n");
            sb.append("depth: " + currentDepth);
            final String content = sb.toString();

            // send email
            LOG.debug("sending report mail");
            _mailSender.sendMail(subject, content, attachment);
        }
    }
}
