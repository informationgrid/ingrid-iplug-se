/*
 * **************************************************-
 * ingrid-iplug-se-nutch
 * ==================================================
 * Copyright (C) 2014 - 2025 wemove digital solutions GmbH
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
package de.ingrid.iplug.se.nutch.statistics;

import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.WritableComparable;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.MultipleInputs;
import org.apache.hadoop.mapreduce.lib.input.SequenceFileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.SequenceFileOutputFormat;
import org.apache.hadoop.util.StringUtils;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.apache.nutch.crawl.CrawlDatum;
import org.apache.nutch.crawl.CrawlDb;
import org.apache.nutch.util.NutchConfiguration;
import org.apache.nutch.util.NutchJob;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

/**
 * @author joachim
 * 
 * Generates a report for all start urls containing dates like last fetch time, status and score in JSON format.
 *
 */
public class StartUrlStatusReport extends Configured implements Tool {

    private static final Logger LOG = LoggerFactory.getLogger(StartUrlStatusReport.class.getName());

    public static class StartUrlMapper extends Mapper<WritableComparable<?>, Text, Text, CrawlDatum> {

        @Override
        public void map(WritableComparable<?> key, Text value, Mapper<WritableComparable<?>, Text, Text, CrawlDatum>.Context context) throws IOException, InterruptedException {
            String url = value.toString().trim(); // value is line of text

            if (url.length() == 0 || url.startsWith("#")) {
                /* Ignore line that start with # */
                return;
            }
            value.set(url); // collect it
            CrawlDatum datum = new CrawlDatum();
            datum.setStatus(CrawlDatum.STATUS_INJECTED);
            context.write(value, datum);
        }
    }
    
    public static class CrawlDbMapper extends Mapper<Text, CrawlDatum, Text, CrawlDatum> {

        @Override
        public void map(Text key, CrawlDatum value, Mapper<Text, CrawlDatum, Text, CrawlDatum>.Context context) throws IOException, InterruptedException {
            context.write(key, value);
        }
    }    

    public static class StartUrlReducer extends Reducer<Text, CrawlDatum, Text, CrawlDatum> {

        @Override
        public void reduce(Text key, Iterable<CrawlDatum> values, Reducer<Text, CrawlDatum, Text, CrawlDatum>.Context context) throws IOException, InterruptedException {
            boolean matchStartUrl = false;
            CrawlDatum datum = null;
            for (CrawlDatum val : values) {
                if (val.getStatus() == CrawlDatum.STATUS_INJECTED) {
                    matchStartUrl = true;
                } else if (CrawlDatum.hasDbStatus(val)) {
                    datum = (CrawlDatum) val.clone();
                }
            }
            if (!matchStartUrl || datum == null) {
                return;
            }
            context.write(key, datum);
        }
    }


    public StartUrlStatusReport() {
    }

    public void startUrlReport(Path crawldb, Path urlDir, Path outputDir) throws IOException, InterruptedException, ClassNotFoundException {

        Path out = new Path(outputDir, "statistic/starturlreport");

        FileSystem fs = FileSystem.get(getConf());
        if (fs.exists(out)) {
            fs.delete(out, true);
        }

        LOG.info("Start start url report.");

        // filter out crawldb entries that match starturls
        Job job = NutchJob.getInstance(getConf());
        job.setJobName("starturlreport");

        MultipleInputs.addInputPath(job, urlDir, TextInputFormat.class, StartUrlMapper.class);
        MultipleInputs.addInputPath(job, new Path(crawldb, CrawlDb.CURRENT_NAME), SequenceFileInputFormat.class, CrawlDbMapper.class);

        job.setReducerClass(StartUrlReducer.class);
        job.setOutputFormatClass(SequenceFileOutputFormat.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(CrawlDatum.class);

        String id = Integer.toString(new Random().nextInt(Integer.MAX_VALUE));
        String name = "starturlreport-temp-" + id;
        Path startUrlReportEntries = new Path(getConf().get("hadoop.temp.dir", "."), name);
        FileOutputFormat.setOutputPath(job, startUrlReportEntries);

        try {
            boolean success = job.waitForCompletion(true);
            if (!success) {
                String message = "Job did not succeed, job status:"
                        + job.getStatus().getState() + ", reason: "
                        + job.getStatus().getFailureInfo();
                LOG.error(message);
                throw new RuntimeException(message);
            }
        } catch (IOException | InterruptedException | ClassNotFoundException e) {
            LOG.error("Job failed: {}", e);
            fs.delete(startUrlReportEntries, true);
            throw e;
        }

        SequenceFile.Reader reader = null;
        BufferedWriter br = null;
        try {

            reader = new SequenceFile.Reader(getConf(), SequenceFile.Reader.file(new Path(startUrlReportEntries, "part-r-00000")));
            Text key = new Text();
            CrawlDatum value = new CrawlDatum();
            
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date date = new Date();

            Path file = new Path(out, "data.json");
            OutputStream os = fs.create(file);
            br = new BufferedWriter(new OutputStreamWriter(os, StandardCharsets.UTF_8));
            JSONArray array = new JSONArray();
            while (reader.next(key, value)) {

                JSONObject jsn = new JSONObject();
                jsn.put("url", key.toString());
                long fetchTime;
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
