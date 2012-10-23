package org.apache.nutch.util;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.mapred.JobClient;
import org.apache.hadoop.mapred.JobConf;

public class SyncUtil {
    private static final Log LOG = LogFactory.getLog(SyncUtil.class);

    public static void syncJobRunDeprecared(JobConf job) throws IOException {
        long start = System.currentTimeMillis();
        synchronized (JobClient.class) {
            LOG.debug("sync duration in ms: " + String.valueOf(System.currentTimeMillis()-start));
            JobClient.runJob(job);
        }
        
    }
}
