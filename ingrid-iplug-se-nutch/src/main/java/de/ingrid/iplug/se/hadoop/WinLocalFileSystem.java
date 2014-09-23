package de.ingrid.iplug.se.hadoop;

import java.io.IOException;

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

    /**
         *
         *
         */
    public WinLocalFileSystem() {
        super();
        System.err.println("Patch for HADOOP-7682: " + "Instantiating workaround file system");
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
            System.err.println("Patch for HADOOP-7682: " + "Ignoring IOException setting permission for path \"" + path + "\": " + e.getMessage());
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
