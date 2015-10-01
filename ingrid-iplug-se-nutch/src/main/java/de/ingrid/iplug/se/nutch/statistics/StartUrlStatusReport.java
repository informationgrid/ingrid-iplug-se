/*
 * **************************************************-
 * ingrid-iplug-se-nutch
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
package de.ingrid.iplug.se.nutch.statistics;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Random;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.WritableComparable;
import org.apache.hadoop.mapred.FileOutputFormat;
import org.apache.hadoop.mapred.JobClient;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.Mapper;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reducer;
import org.apache.hadoop.mapred.Reporter;
import org.apache.hadoop.mapred.SequenceFileInputFormat;
import org.apache.hadoop.mapred.SequenceFileOutputFormat;
import org.apache.hadoop.mapred.TextInputFormat;
import org.apache.hadoop.mapred.lib.MultipleInputs;
import org.apache.hadoop.util.StringUtils;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.apache.nutch.crawl.CrawlDatum;
import org.apache.nutch.crawl.CrawlDb;
import org.apache.nutch.util.NutchConfiguration;
import org.apache.nutch.util.NutchJob;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;

/**
 * @author joachim
 * 
 * Generates a report for all start urls containing dates like last fetch time, status and score in JSON format.
 *
 */
public class StartUrlStatusReport extends Configured implements Tool {

    private static final Log LOG = LogFactory.getLog(StartUrlStatusReport.class.getName());

    public static class StartUrlMapper implements Mapper<WritableComparable<?>, Text, Text, CrawlDatum> {

        public void configure(JobConf job) {
        }

        public void close() {
        }

        public void map(WritableComparable<?> key, Text value, OutputCollector<Text, CrawlDatum> output, Reporter reporter) throws IOException {
            String url = value.toString().trim(); // value is line of text

            if (url != null && (url.length() == 0 || url.startsWith("#"))) {
                /* Ignore line that start with # */
                return;
            }

            if (url != null) {
                value.set(url); // collect it
                CrawlDatum datum = new CrawlDatum();
                datum.setStatus(CrawlDatum.STATUS_INJECTED);

                output.collect(value, datum);
            }
        }
    }
    
    public static class CrawlDbMapper implements Mapper<Text, CrawlDatum, Text, CrawlDatum> {

        public void configure(JobConf job) {
        }

        public void close() {
        }

        public void map(Text key, CrawlDatum value, OutputCollector<Text, CrawlDatum> output, Reporter reporter) throws IOException {
            
            output.collect(key, value);
        }
    }    

    public static class StartUrlReducer implements Reducer<Text, CrawlDatum, Text, CrawlDatum> {

        public void configure(JobConf job) {
        }

        public void close() {
        }

        public void reduce(Text key, Iterator<CrawlDatum> values, OutputCollector<Text, CrawlDatum> output, Reporter reporter) throws IOException {
            boolean matchStartUrl = false;
            CrawlDatum datum = null;
            while (values.hasNext()) {
                CrawlDatum val = values.next();
                if (val.getStatus() == CrawlDatum.STATUS_INJECTED) {
                    matchStartUrl = true;
                } else if (CrawlDatum.hasDbStatus(val)) {
                    datum = (CrawlDatum) val.clone();
                }
            }
            if (!matchStartUrl || datum == null) {
                return;
            }
            output.collect(key, datum);
        }
    }

    public StartUrlStatusReport(Configuration configuration) {
        super(configuration);
    }

    public StartUrlStatusReport() {
    }

    public void startUrlReport(Path crawldb, Path urlDir, Path outputDir) throws IOException {

        Path out = new Path(outputDir, "statistic/starturlreport");

        FileSystem fs = FileSystem.get(getConf());
        if (fs.exists(out)) {
            fs.delete(out, true);
        }

        LOG.info("Start start url report.");

        // filter out crawldb entries that match starturls
        JobConf job = new NutchJob(getConf());
        job.setJobName("starturlreport");

        MultipleInputs.addInputPath(job, urlDir, TextInputFormat.class, StartUrlMapper.class);
        MultipleInputs.addInputPath(job, new Path(crawldb, CrawlDb.CURRENT_NAME), SequenceFileInputFormat.class, CrawlDbMapper.class);

        job.setReducerClass(StartUrlReducer.class);
        job.setOutputFormat(SequenceFileOutputFormat.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(CrawlDatum.class);

        String id = Integer.toString(new Random().nextInt(Integer.MAX_VALUE));
        String name = "starturlreport-temp-" + id;
        Path startUrlReportEntries = new Path(getConf().get("hadoop.temp.dir", "."), name);
        FileOutputFormat.setOutputPath(job, startUrlReportEntries);

        JobClient.runJob(job);

        SequenceFile.Reader reader = null;
        BufferedWriter br = null;
        try {

            reader = new SequenceFile.Reader(fs, new Path(startUrlReportEntries, "part-00000"), getConf());
            Text key = new Text();
            CrawlDatum value = new CrawlDatum();
            
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date date = new Date();

            Path file = new Path(out, "data.json");
            OutputStream os = fs.create(file);
            br = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
            JSONArray array = new JSONArray();
            while (reader.next(key, value)) {

                JSONObject jsn = new JSONObject();
                jsn.put("url", key.toString());
                long fetchTime = 0;
                if (value.getStatus() == CrawlDatum.STATUS_DB_UNFETCHED) {
                    fetchTime = value.getFetchTime();
                } else {
                    fetchTime = value.getFetchTime() - ((long)value.getFetchInterval()) * 1000;
                }
                date.setTime(fetchTime);
                jsn.put("lastFetchTime", df.format(date));
                jsn.put("status", CrawlDatum.statNames.get(value.getStatus()));
                jsn.put("score", value.getScore());
                jsn.put("fetchInterval", value.getFetchInterval());
                array.put(jsn);
            }
            br.write(array.toString());
        } catch (Exception e) {
            LOG.error("Error creating JSON from start url report", e);
            e.printStackTrace();
        } finally {
            if (reader != null) {
                reader.close();
            }
            if (br != null) {
                br.close();
            }
        }

        fs.delete(startUrlReportEntries, true);
    }

    public static void main(String[] args) throws Exception {
        int res = ToolRunner.run(NutchConfiguration.create(), new StartUrlStatusReport(), args);
        System.exit(res);
    }

    @Override
    public int run(String[] args) throws Exception {
        if (args.length < 3) {
            System.err.println("Usage: StartUrlStatusReport <crawldb> <url_dir> <output_dir>");
            System.err.println("       The statistic will be written to <output_dir>/statistic/starturlreport.");

            return -1;
        }
        try {
            startUrlReport(new Path(args[0]), new Path(args[1]), new Path(args[2]));
            return 0;
        } catch (Exception e) {
            LOG.error("StartUrlStatusReport: " + StringUtils.stringifyException(e));
            return -1;
        }
    }

}
