package de.ingrid.iplug.se;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.apache.hadoop.fs.FileSystem;

public class SearchableIndexFinder {

    private static final Logger LOG = Logger.getLogger(SearchableIndexFinder.class.getName());
    
    public List<File> findIndices(final FileSystem fileSystem, File folder) throws IOException {
        List<File> indices = new ArrayList<File>();
        List<File> segments = new ArrayList<File>();
        List<File> allFolders = new ArrayList<File>();

        findAllFolders(fileSystem, folder, allFolders);

        for (File aFolder : allFolders) {
            extractSegments(fileSystem, aFolder, segments);
        }

        for (File segment : segments) {
            extractIndices(fileSystem, segment, indices);
        }

        return indices;

    }

    private void findAllFolders(final FileSystem fileSystem, File folder, List<File> list) {
        File[] directories = folder.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                boolean ret = false;
                try {
                    ret = fileSystem.isDirectory(pathname);
                } catch (IOException e) {
                    LOG.warning(e.getMessage());
                }
                return ret;
            }
        });
        for (File directory : directories) {
            list.add(directory);
            findAllFolders(fileSystem, directory, list);
        }
    }

    private void extractSegments(final FileSystem fileSystem, File folder, List<File> segmentList) throws IOException {
        File[] segments = fileSystem.listFiles(folder, (new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                boolean ret = false;
                try {
                    boolean isDirectory = fileSystem.isDirectory(pathname);
                    File[] searchDone = new File[] {};
                    if (isDirectory) {
                        searchDone = fileSystem.listFiles(pathname, new FileFilter() {
                            @Override
                            public boolean accept(File pathname) {
                                boolean ret = false;
                                try {
                                    boolean isFile = fileSystem.isFile(pathname);
                                    boolean isSearchable = pathname.getName().equals("search.done");
                                    ret = isFile && isSearchable;
                                } catch (Exception e) {
                                    LOG.warning(e.getMessage());
                                }
                                return ret;
                            }
                        });
                    }
                    ret = searchDone.length == 1;
                    if (ret) {
                        LOG.info("found search.done file: " + searchDone[0].getAbsolutePath());
                    }
                } catch (Exception e) {
                    LOG.warning(e.getMessage());
                }
                return ret;
            }
        }));
        for (File segment : segments) {
            segmentList.add(segment);
        }
    }

    private void extractIndices(final FileSystem fileSystem, File segment, List<File> indices) throws IOException {
        File index = new File(segment, "index");
        File[] parts = fileSystem.listFiles(index, new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                boolean ret = false;
                try {
                    ret = fileSystem.isDirectory(pathname) && pathname.getName().startsWith("part");
                } catch (IOException e) {
                    LOG.warning(e.getMessage());
                }
                return ret;
            }
        });
        for (File part : parts) {
            LOG.info("found index-part: " + part.getAbsolutePath());
            indices.add(part);
        }
    }

}
