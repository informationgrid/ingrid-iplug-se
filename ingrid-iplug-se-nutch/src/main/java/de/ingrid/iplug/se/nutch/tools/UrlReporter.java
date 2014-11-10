/*
 * **************************************************-
 * ingrid-iplug-se-nutch
 * ==================================================
 * Copyright (C) 2014 wemove digital solutions GmbH
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
package de.ingrid.iplug.se.nutch.tools;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

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
    
    public static final String CODES = "report.codes";

    public static final String REPORT = "report";

    protected static final Log LOG = LogFactory.getLog(UrlReporter.class.getName());

    private final FileSystem _fileSystem;
    
    public static class ReporterMapper implements Mapper<Text, CrawlDatum, IntWritable, Text> {
        
        private CodeFilter _filter;

        @Override
        public void map(final Text key, final CrawlDatum value, final OutputCollector<IntWritable, Text> output,
                final Reporter reporter) throws IOException {
            // get response code
            final Writable responseCode = value.getMetaData().get(RESPONSE_CODE);
            if (responseCode != null && responseCode instanceof IntWritable) {
                final IntWritable code = (IntWritable) responseCode;
                // only report if code is not success
                if (_filter.accept(code.get())) {
                    output.collect(code, key);
                }
            }
        }

        @Override
        public void configure(final JobConf conf) {
            _filter = CodeFilter.parse(conf.get(CODES, "100-505"));
        }

        @Override
        public void close() throws IOException {
        }
        
        private static class CodeFilter {
            private class Range {
                public int from;
                public int to;
                public Range(final int from, final int to) {
                    this.from = from;
                    this.to = to;
                }
                public boolean inRange(final int code) {
                    return code >= from  && code <= to;
                }
                @Override
                public boolean equals(final Object obj) {
                    if (obj instanceof CodeFilter) {
                        final Range range = (Range) obj;
                        return range.from == from && range.to == to;
                    }
                    return false;
                }
            }
            
            private Set<Integer> _codes = new HashSet<Integer>();
            private Set<Range> _ranges = new HashSet<Range>();
            
            public static CodeFilter parse(final String codeString) {
                final CodeFilter filter = new CodeFilter();
            
                if (codeString != null) {
                    final String trimmed = codeString.trim();
                    if (trimmed.length() >= 0) {
                        for (final String element : trimmed.split(",")) {
                            try {
                                final String[] split = element.trim().split("-");
                                if (split.length > 1) {
                                    filter.addRange(Integer.parseInt(split[0].trim()), Integer.parseInt(split[1].trim()));
                                } else {
                                    filter.addCode(Integer.parseInt(split[0].trim()));
                                }
                            } catch (final Exception e) {
                            }
                        }
                    }
                }
                
                return filter;
            }
            
            public boolean accept(final int code) {
                if (_codes.contains(code)) {
                    return true;
                }
                for (final Range range : _ranges) {
                    if (range.inRange(code)) {
                        return true;
                    }
                }
                return false;
            }
            
            public void addCode(final int code) {
                _codes.add(code);
            }
            
            public void addRange(final int from, final int to) {
                _ranges.add(new Range(from, to));
            }
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
        job.set(CODES, getConf().get(CODES));
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
