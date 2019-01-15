/*
 * **************************************************-
 * ingrid-iplug-se-iplug
 * ==================================================
 * Copyright (C) 2014 - 2019 wemove digital solutions GmbH
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
package de.ingrid.iplug.se.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import de.ingrid.iplug.se.SEIPlug;
import de.ingrid.iplug.se.db.UrlHandler;
import de.ingrid.iplug.se.db.model.Metadata;
import de.ingrid.iplug.se.db.model.Url;
import edu.emory.mathcs.backport.java.util.Arrays;

public class FileUtils {

    private static Logger log = Logger.getLogger(FileUtils.class);

    private static final Pattern REGEXP_SPECIAL_CHARS = Pattern.compile("([\\\\*+\\[\\](){}\\$.?\\^|])");

    public static void removeRecursive(Path path) throws IOException {
        removeRecursive(path, ".*");
    }

    public static void removeRecursive(Path path, final String pattern) throws IOException {

        final PathMatcher matcher = FileSystems.getDefault().getPathMatcher("regex:" + pattern);

        if (!Files.exists(path))
            return;

        Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                if (pattern != null && matcher.matches(file.getFileName())) {
                    Files.delete(file);
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                // try to delete the file anyway, even if its attributes
                // could not be read, since delete-only access is
                // theoretically possible
                if (pattern != null && matcher.matches(file.getFileName())) {
                    Files.delete(file);
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                if (exc == null) {
                    if (matcher.matches(dir.getFileName())) {
                        if (dir.toFile().list().length > 0) {
                            // remove even if not empty
                            FileUtils.removeRecursive(dir);
                        } else {
                            Files.delete(dir);
                        }
                    }
                    return FileVisitResult.CONTINUE;
                } else {
                    // directory iteration failed; propagate exception
                    throw exc;
                }
            }

        });
    }

    public static void copyDirectories(Path source, Path destination) throws IOException {

        Files.walkFileTree(source, new CopyVisitor(source, destination));

    }

    public static void writeToFile(Path path, String name, List<String> listContent) throws IOException {
        if (path != null) {
            Files.createDirectories(path);
            if (listContent != null) {
                Writer writer = new FileWriter(path.resolve(name).toString());
                BufferedWriter bWriter = new BufferedWriter(writer);
                for (String content : listContent) {
                    bWriter.write(content);
                    bWriter.newLine();
                }
                bWriter.close();
            } else {
                log.error("Content is null!");
            }
        }
    }

    private static class CopyVisitor extends SimpleFileVisitor<Path> {
        private Path fromPath;
        private Path toPath;
        private StandardCopyOption copyOption = StandardCopyOption.REPLACE_EXISTING;

        CopyVisitor(Path fromPath, Path toPath) {
            this.fromPath = fromPath;
            this.toPath = toPath;
        }

        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
            Path targetPath = toPath.resolve(fromPath.relativize(dir));
            if (!Files.exists(targetPath)) {
                Files.createDirectory(targetPath);
            }
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            Files.copy(file, toPath.resolve(fromPath.relativize(file)), copyOption);
            return FileVisitResult.CONTINUE;
        }
    }

    @SuppressWarnings("unchecked")
    public static void prepareCrawl(String name) throws IOException {
        String workDir = SEIPlug.conf.getInstancesDir() + "/" + name;

        // get all urls belonging to the given instance
        List<Url> urls = UrlHandler.getUrlsByInstance(name);
        Map<String, Map<String, List<String>>> startUrls = new HashMap<String, Map<String, List<String>>>();
        List<String> limitUrls = new ArrayList<String>();
        List<String> excludeUrls = new ArrayList<String>();

        for (Url url : urls) {

            if (url.getUrl() == null)
                continue;

            // ensure trailing slash on domain only urls
            // skip invalid urls
            try {
                URL tmpUrl = new URL(url.getUrl());
                if (tmpUrl.getPath().isEmpty() && tmpUrl.getQuery() == null) {
                    url.setUrl(url.getUrl() + "/");
                }
            } catch (Exception e) {
                log.error("Invalid start url detected. Skipping: " + url.getUrl());
                continue;
            }

            Map<String, List<String>> metadata = new HashMap<String, List<String>>();
            for (Metadata meta : url.getMetadata()) {
                List<String> metaValues = metadata.get(meta.getMetaKey());
                if (metaValues == null) {
                    metaValues = new ArrayList<String>();
                    metadata.put(meta.getMetaKey(), metaValues);
                }
                metaValues.add(meta.getMetaValue());
            }

            startUrls.put(url.getUrl().trim(), metadata);
            for (String limit : url.getLimitUrls()) {
                limitUrls.add(checkForRegularExpressions(limit.trim()));
            }
            for (String exclude : url.getExcludeUrls()) {
                excludeUrls.add(checkForRegularExpressions(exclude.trim()));
            }
        }

        // output urls and metadata
        String[] startUrlsValue = startUrls.keySet().toArray(new String[0]);
        FileUtils.writeToFile(Paths.get(workDir, "urls", "start").toAbsolutePath(), "seed.txt", Arrays.asList(startUrlsValue));

        List<String> metadataValues = new ArrayList<String>();
        for (String start : startUrlsValue) {
            Map<String, List<String>> metas = startUrls.get(start);

            String metasConcat = checkForRegularExpressions(start);
            for (String key : metas.keySet()) {
                metasConcat += "\t" + key + ":\t" + StringUtils.join(metas.get(key), "\t");
            }
            metadataValues.add(metasConcat);
        }
        
        // TODO: add depending fields to metadata file (REDMINE-94)

        FileUtils.writeToFile(Paths.get(workDir, "urls", "metadata").toAbsolutePath(), "seed.txt", metadataValues);
        FileUtils.writeToFile(Paths.get(workDir, "urls", "limit").toAbsolutePath(), "seed.txt", limitUrls);
        FileUtils.writeToFile(Paths.get(workDir, "urls", "exclude").toAbsolutePath(), "seed.txt", excludeUrls);
    }

    /**
     * Read the content of a file from a path. If the file does not exist, then
     * null is returned.
     * 
     * @param path
     *            is the location of the file to read the content from
     * @return the content of the file if it exists otherwise null
     * @throws IOException
     */
    public static String readFile(Path path) throws IOException {
        if (!path.toFile().exists())
            return null;

        byte[] encoded = Files.readAllBytes(path);
        return new String(encoded, "UTF-8");
    }

    public static String[] getSortedSubDirectories(Path path) {

        String[] directories = path.toFile().list(new FilenameFilter() {
            @Override
            public boolean accept(File current, String name) {
                return new File(current, name).isDirectory();
            }
        });
        if (directories != null) {
            Arrays.sort(directories);
        } else {
            directories = new String[] {};
        }

        return directories;

    }

    /**
     * Read the last N lines of a file
     * 
     * @param file
     * @param lines
     * @return
     */
    public static String tail(File file, int lines) {
        java.io.RandomAccessFile fileHandler = null;
        try {
            fileHandler = new java.io.RandomAccessFile(file, "r");
            long fileLength = fileHandler.length() - 1;
            StringBuilder sb = new StringBuilder();
            int line = 0;

            for (long filePointer = fileLength; filePointer != -1; filePointer--) {
                fileHandler.seek(filePointer);
                int readByte = fileHandler.readByte();

                if (readByte == 0xA) {
                    line = line + 1;
                    if (line == lines) {
                        if (filePointer == fileLength) {
                            continue;
                        }
                        break;
                    }
                } else if (readByte == 0xD) {
                    line = line + 1;
                    if (line == lines) {
                        if (filePointer == fileLength - 1) {
                            continue;
                        }
                        break;
                    }
                }
                sb.append((char) readByte);
            }

            String lastLine = sb.reverse().toString();
            return lastLine;
        } catch (java.io.FileNotFoundException e) {
            e.printStackTrace();
            return null;
        } catch (java.io.IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            if (fileHandler != null)
                try {
                    fileHandler.close();
                } catch (IOException e) {
                }
        }
    }

    public static File[] getInstancesDirs() {
        File[] subDirs = new File[0];
        String dir = SEIPlug.conf.getInstancesDir();
        if (Files.isDirectory(Paths.get(dir))) {
            FileFilter directoryFilter = new FileFilter() {
                public boolean accept(File file) {
                    return file.isDirectory();
                }
            };
            File instancesDirObject = new File(dir);
            subDirs = instancesDirObject.listFiles(directoryFilter);
        }

        return subDirs;
    }

    private static String checkForRegularExpressions(String urlStr) {
        if (urlStr.startsWith("/") && urlStr.endsWith("/")) {
            urlStr = urlStr.substring(1, urlStr.length() - 1);
        } else {
            URL uri;
            try {
                uri = new URL(urlStr);
                if (uri.getPath() != null || uri.getQuery() != null) {
                    Matcher match = REGEXP_SPECIAL_CHARS.matcher((uri.getPath() != null ? uri.getPath() : "") + (uri.getQuery() != null ? "?" + uri.getQuery() : ""));
                    urlStr = uri.getProtocol() + "://" + uri.getHost() + (uri.getPort() > 0 ? ":" + uri.getPort() : "") + match.replaceAll("\\\\$1");
                }
            } catch (MalformedURLException e) {
                log.error("The url pattern: '" + urlStr + "' is not a valid url.");
            }
        }
        return urlStr;
    }

}
