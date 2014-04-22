package de.ingrid.nutch.mail;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.compress.CompressionCodec;
import org.apache.hadoop.io.compress.CompressionCodecFactory;
import org.apache.hadoop.io.compress.CompressionInputStream;

import de.ingrid.nutch.tools.UrlReporter;
import de.ingrid.nutch.util.ZipUtil;

public class MailService extends Configured {

    public static final String TEMP_DIR = "nutch_reports";

    protected static Log LOG = LogFactory.getLog(MailService.class);

    protected static MailService _instance;

    private final MailSender _mailSender;
    
    private MailService(final Configuration configuration) {
        super(configuration);
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
        LOG.debug("moving report data " + report + " to local data " + reportPath);
        fileSystem.copyToLocalFile(report, reportPath);
        final File reportFile = new File(reportPath.toString());
        
        if (reportFile.isDirectory()) {
            File[] listOfFiles = reportFile.listFiles();
            CompressionCodecFactory ccf = new CompressionCodecFactory(getConf());
            for (int i=0; i<listOfFiles.length; i++) {
                LOG.debug("processing decompression of file " + listOfFiles[i].getName());
                Path reportFileWithPath = new Path(reportPath, listOfFiles[i].getName());                
                CompressionCodec codec = ccf.getCodec(reportFileWithPath);
                LOG.debug("codec of file " + reportFileWithPath + " = " + codec);
                if (codec == null) {
                    LOG.debug("codec not found, skipping file " + reportFileWithPath);
                    continue;
                }
                String decompressedFileName = CompressionCodecFactory.removeSuffix(reportFileWithPath.getName(), codec.getDefaultExtension());
                Path decompressedFileWithPath = new Path(reportPath, decompressedFileName);
                LOG.debug("decompressedFile = " + decompressedFileWithPath);

                CompressionInputStream in = null;
                OutputStream out = null;
                File outf = null;
                try {
                    byte[] buffer = new byte[32000];
                    int len;
                    LOG.debug("START: decompressing file " + listOfFiles[i] + " to file " + decompressedFileWithPath);
                    in = codec.createInputStream(new FileInputStream(listOfFiles[i]));
                    outf = new File(decompressedFileWithPath.toString());
                    out = new FileOutputStream(outf);
                    while( 0 < (len = in.read(buffer)) ) {
                        out.write( buffer, 0, len );
                    }
                    LOG.debug("END: decompressing file " + listOfFiles[i] + " to file " + decompressedFileWithPath);
                } finally {
                    if (in != null) {
                       in.close();
                    }
                    if (out != null) {
                       out.close();
                    }
                }
                if (outf != null && outf.exists()) {
                    listOfFiles[i].delete();
                }
            }
        }

        // load attachment
        LOG.debug("creating attachment");
        File attachment = reportFile;
        if (reportFile.exists()) {
            // compress dir
            if (reportFile.isDirectory()) {
                attachment = ZipUtil.zip(reportFile, reportFile);
            }

            // create message
            final String subject = "[CRAWL] URL report for crawl '" + crawlName + "' of segment '" + segmentName + "'";
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
