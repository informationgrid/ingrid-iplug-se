package org.apache.nutch.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ZipUtil {

    public static final int BUFFER_SIZE = 8192;

    protected final static Log LOG = LogFactory.getLog(ZipUtil.class);

    public static File zip(final File in, final File out) throws IOException {
        final File output = prepare(in, out);
        LOG.info("zipping '" + in + "' to '" + out + "'");
        final ZipOutputStream writer = new ZipOutputStream(new FileOutputStream(output));
        compress(in, writer, in.getParent().length() + 1);
        writer.close();
        LOG.debug("zipping done");
        return output;
    }

    private static File prepare(final File in, final File out) {
        if (out.exists()) {
            if (out.isDirectory()) {
                return new File(out.getParent(), in.getName() + ".zip");
            }
        } else {
            final File parent = new File(out.getParent());
            parent.mkdirs();
        }
        return out;
    }

    private static void compress(final File in, final ZipOutputStream writer, final int base) throws IOException {
        if (in.isDirectory()) {
            for (final File file : in.listFiles()) {
                compress(file, writer, base);
            }
        } else {
            writer.putNextEntry(createEntry(in, base));
            final FileInputStream reader = new FileInputStream(in);
            final byte[] b = new byte[BUFFER_SIZE];
            for (int l; (l = reader.read(b)) != -1;) {
                writer.write(b, 0, l);
            }
            reader.close();
        }
    }

    private static ZipEntry createEntry(final File current, final int base) {
        final String entry = current.getPath().substring(base);
        LOG.debug("creating entry '" + entry + "'");
        return new ZipEntry(entry);
    }
}