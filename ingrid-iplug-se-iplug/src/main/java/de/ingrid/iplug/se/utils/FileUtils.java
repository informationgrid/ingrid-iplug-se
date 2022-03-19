/*
 * **************************************************-
 * ingrid-iplug-se-iplug
 * ==================================================
 * Copyright (C) 2014 - 2022 wemove digital solutions GmbH
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

import de.ingrid.iplug.se.SEIPlug;
import de.ingrid.iplug.se.db.UrlHandler;
import de.ingrid.iplug.se.db.model.Metadata;
import de.ingrid.iplug.se.db.model.Url;
import de.ingrid.utils.tool.UrlTool;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.io.*;
import java.net.*;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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
    public static void prepareCrawl(String name) throws IOException, URISyntaxException {
        String workDir = SEIPlug.conf.getInstancesDir() + "/" + name;

        // get all urls belonging to the given instance
        List<Url> urls = UrlHandler.getUrlsByInstance(name);
        Map<String, Map<String, List<String>>> metadataEntries = new HashMap<String, Map<String, List<String>>>();
        List<String> startUrls = new ArrayList<String>();
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

            startUrls.add(FileUtils.encodeIdnAndUri(url.getUrl()));

            for (String limit : url.getLimitUrls()) {
                String limitPattern = checkForRegularExpressions(limit.trim());
                limitUrls.add(limitPattern);
                metadataEntries.put(limitPattern, metadata);
            }
            for (String exclude : url.getExcludeUrls()) {
                excludeUrls.add(checkForRegularExpressions(exclude.trim()));
            }
        }

        List<String> metadataValues = new ArrayList<String>();
        metadataEntries.forEach((limitPattern, metaValues) -> {
            metaValues.forEach((key, value) -> {
                metadataValues.add(limitPattern + "\t" + key + ":\t" + StringUtils.join(value, "\t"));
            });
        });

        FileUtils.writeToFile(Paths.get(workDir, "urls", "start").toAbsolutePath(), "seed.txt", startUrls);
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
            log.error("Error finding file", e);
            return null;
        } catch (java.io.IOException e) {
            log.error("Error by tail command", e);
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

    /**
     * Checks for regular expression by testing for leading and trailing '/'.
     * If no regular expression was found:
     *
     * <ul>
     *  <li>the urls domain is IDNA 2008 encoded and the</li>
     *  <li>the path, query and anchor is URI encoded</li>
     *  <li>all special regular expresseion characters are escaped</li>
     * </ul>
     *
     * @param urlStr
     * @return
     */
    public static String checkForRegularExpressions(String urlStr) {
        if (urlStr.startsWith("/") && urlStr.endsWith("/")) {
            urlStr = urlStr.substring(1, urlStr.length() - 1);
        } else {
            URL uri;
            try {
                uri = new URL(UrlTool.getEncodedUnicodeUrl(urlStr));
                if (uri.getPath() != null || uri.getQuery() != null) {
                    Matcher match = REGEXP_SPECIAL_CHARS.matcher((uri.getPath() != null ? uri.getPath() : "") + (uri.getQuery() != null ? "?" + uri.getQuery() : "") + (uri.getRef() != null ? "#" + uri.getRef() : ""));
                    urlStr = uri.getProtocol() + "://" + uri.getHost() + (uri.getPort() > 0 ? ":" + uri.getPort() : "") + match.replaceAll("\\\\$1");
                }
            } catch (MalformedURLException | URISyntaxException e) {
                log.error("The url pattern: '" + urlStr + "' is not a valid url.");
            }
        }
        return urlStr;
    }

    private static List<String> encodeIdnAndUri(List<String> urls) {
        return urls.stream().map(c -> {
            return encodeIdnAndUri(c);
        }).collect(Collectors.toList());
    }

    private static String encodeIdnAndUri(String url) {
        try {
            return UrlTool.getEncodedUnicodeUrl(url);
        } catch (MalformedURLException e) {
            log.error("Error escaping URL " + url, e);
            throw new RuntimeException(e);
        } catch (URISyntaxException e) {
            log.error("Error escaping URL " + url, e);
            throw new RuntimeException(e);
        }
    }


}
