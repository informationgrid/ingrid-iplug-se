/*
 * **************************************************-
 * ingrid-iplug-se-nutch
 * ==================================================
 * Copyright (C) 2014 - 2020 wemove digital solutions GmbH
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
package de.ingrid.iplug.se.hadoop;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.fs.LocalFileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.permission.FsPermission;

/**
 * Fix for running Hadoop 1.0.3 on Windows according to the following
 * suggestion:
 * 
 * https://issues.apache.org/jira/browse/HADOOP-7682?page=com.atlassian.jira.
 * plugin
 * .system.issuetabpanels:comment-tabpanel&focusedCommentId=13440120#comment
 * -13440120
 * 
 * @author Todd Fast
 */
public class WinLocalFileSystem extends LocalFileSystem {

    
    public final static Log log = LogFactory.getLog(WinLocalFileSystem.class);
    /**
         *
         *
         */
    public WinLocalFileSystem() {
        super();
        if (log.isDebugEnabled()) {
            log.debug("Patch for HADOOP-7682: Instantiating workaround file system");
        }
    }

    /**
     * Delegates to <code>super.mkdirs(Path)</code> and separately calls
     * <code>this.setPermssion(Path,FsPermission)</code>
     * 
     */
    @Override
    public boolean mkdirs(Path path, FsPermission permission) throws IOException {
        boolean result = super.mkdirs(path);
        this.setPermission(path, permission);
        return result;
    }

    /**
     * Ignores IOException when attempting to set the permission
     * 
     */
    @Override
    public void setPermission(Path path, FsPermission permission) throws IOException {
        try {
            super.setPermission(path, permission);
        } catch (IOException e) {
            if (log.isDebugEnabled()) {
                log.debug("Patch for HADOOP-7682: " + "Ignoring IOException setting permission for path \"" + path + "\": " + e.getMessage());
            }
        }
    }

    @Override
    public boolean rename(Path src, Path dst) throws IOException {
        if (exists(dst)) {
            delete(dst, true);
        }
        return super.rename(src, dst);
    }
}
