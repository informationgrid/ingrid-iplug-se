/*
 * **************************************************-
 * ingrid-iplug-se-iplug
 * ==================================================
 * Copyright (C) 2014 - 2024 wemove digital solutions GmbH
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
package de.ingrid.iplug.se.nutchController;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import de.ingrid.iplug.se.Configuration;
import de.ingrid.iplug.se.SEIPlug;
import de.ingrid.iplug.se.utils.FileUtils;
import de.ingrid.iplug.se.webapp.container.Instance;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;

public class NutchUrlTesterTest {

    @Test
    public void test() throws IOException, InterruptedException {

        FileUtils.removeRecursive(Paths.get("test-instances"));

        Configuration configuration = new Configuration();
        configuration.setInstancesDir("test-instances");
        configuration.nutchCallJavaOptions = java.util.Arrays.asList("-Dhadoop.log.file=hadoop.log", "-Dfile.encoding=UTF-8");
        SEIPlug.conf = configuration;

        Instance instance = new Instance();
        instance.setName("test");
        instance.setWorkingDirectory(SEIPlug.conf.getInstancesDir() + "/test");

        Path conf = Paths.get(SEIPlug.conf.getInstancesDir(), "test", "conf").toAbsolutePath();
        Files.createDirectories(conf);

        FileUtils.copyDirectories(Paths.get("apache-nutch-runtime/runtime/local/conf").toAbsolutePath(), conf);

        NutchProcessFactory npf = new NutchProcessFactory();
        NutchProcess process = npf.getUrlTesterProcess(instance, "http://www.google.de");

        process.start();

        long start = System.currentTimeMillis();
        Thread.sleep(100);
        assertThat("Status is RUNNING", process.getStatus(), is(NutchProcess.STATUS.RUNNING));
        while ((System.currentTimeMillis() - start) < 300000) {
            Thread.sleep(1000);
            if (process.getStatus() != NutchProcess.STATUS.RUNNING) {
                break;
            }
        }
        assertThat("Status is FINISHED", process.getStatus(), is(NutchProcess.STATUS.FINISHED));
        System.out.println(process.getConsoleOutput());
        assertThat("Console Output is not empty", process.getConsoleOutput().length() > 0, is(true));

        FileUtils.removeRecursive(Paths.get("test-instances"));
    }

}
