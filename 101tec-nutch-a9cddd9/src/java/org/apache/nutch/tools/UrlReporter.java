package org.apache.nutch.tools;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapred.FileInputFormat;
import org.apache.hadoop.mapred.FileOutputFormat;
import org.apache.hadoop.mapred.JobClient;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.Mapper;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reporter;
import org.apache.hadoop.mapred.SequenceFileInputFormat;
import org.apache.hadoop.mapred.TextOutputFormat;
import org.apache.nutch.crawl.CrawlDatum;

public class UrlReporter extends Configured {

    public static final Text RESPONSE_CODE = new Text("response_code");

    public static final String REPORT = "report";

    protected static final Log LOG = LogFactory.getLog(UrlReporter.class.getName());

    private final FileSystem _fileSystem;

    public static class ReporterMapper implements Mapper<Text, CrawlDatum, IntWritable, Text> {

        @Override
        public void map(final Text key, final CrawlDatum value, final OutputCollector<IntWritable, Text> output,
                final Reporter reporter) throws IOException {
            // get response code
            final Writable responseCode = value.getMetaData().get(RESPONSE_CODE);
            if (responseCode != null && responseCode instanceof IntWritable) {
                final IntWritable code = (IntWritable) responseCode;
                // only report if code is not success
                if (code.get() != 200) {
                    output.collect(code, key);
                }
            }
        }

        @Override
        public void configure(final JobConf conf) {
        }

        @Override
        public void close() throws IOException {
        }
    }

    public UrlReporter(final Configuration configuration) throws IOException {
        super(configuration);
        _fileSystem = FileSystem.get(getConf());
    }

    public void analyze(final Path segment) throws IOException {
        LOG.info("START :: analyzing segment '" + segment + "' for report");

        // delete old report
        LOG.info("deleting old report");
        final Path output = new Path(segment, REPORT);
        _fileSystem.delete(output, true);

        // create job
        final JobConf job = new JobConf();
        job.setJobName("segment-report-" + segment.getName());

        FileInputFormat.addInputPath(job, new Path(segment, CrawlDatum.FETCH_DIR_NAME));
        job.setInputFormat(SequenceFileInputFormat.class);
        job.setMapperClass(ReporterMapper.class);

        FileOutputFormat.setOutputPath(job, output);
        job.setOutputFormat(TextOutputFormat.class);
        job.setOutputKeyClass(IntWritable.class);
        job.setOutputValueClass(Text.class);

        // run job
        LOG.info("start running report job for '" + segment + "'");
        JobClient.runJob(job);
    }
}
