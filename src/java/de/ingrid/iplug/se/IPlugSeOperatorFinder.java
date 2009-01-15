package de.ingrid.iplug.se;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.LocalFileSystem;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiReader;
import org.apache.lucene.search.FieldCache;

import de.ingrid.utils.PlugDescription;
import de.ingrid.utils.metadata.IPlugOperatorFinder;

public class IPlugSeOperatorFinder implements IPlugOperatorFinder {

    private static final Logger LOG = Logger.getLogger(IPlugSeOperatorFinder.class.getName());

    private FileSystem _fileSystem = null;

    private File _workinDirectory;

    public List<File> findIndices(File folder) throws IOException {
        _fileSystem = _fileSystem == null ? new LocalFileSystem(new Configuration()) : _fileSystem;
        List<File> indices = new ArrayList<File>();
        List<File> segments = new ArrayList<File>();
        List<File> allFolders = new ArrayList<File>();

        findAllFolders(_fileSystem, folder, allFolders);

        for (File aFolder : allFolders) {
            extractSegments(_fileSystem, aFolder, segments);
        }

        for (File segment : segments) {
            extractIndices(_fileSystem, segment, indices);
        }

        return indices;

    }

    public void setFileSystem(FileSystem fileSystem) {
        _fileSystem = fileSystem;
    }

    public FileSystem getFileSystem() {
        return _fileSystem;
    }

    public Set<String> findIndexValues(File workingFolder, String indexFieldName) throws IOException {
        List<File> indices = findIndices(workingFolder);
        IndexReader[] readers = new IndexReader[indices.size()];
        for (int i = 0; i < indices.size(); i++) {
            readers[i] = IndexReader.open(indices.get(i));
        }
        MultiReader multiReader = new MultiReader(readers);
        String[] values = FieldCache.DEFAULT.getStrings(multiReader, indexFieldName);
        multiReader.close();

        Set<String> valueSet = new HashSet<String>(Arrays.asList(values));
        valueSet.remove(null);
        return valueSet;
    }

    @Override
    public Set<String> findPartner() throws IOException {
        return findIndexValues(_workinDirectory, "partner");
    }

    @Override
    public Set<String> findProvider() throws IOException {
        return findIndexValues(_workinDirectory, "provider");
    }

    @Override
    public void configure(PlugDescription plugDescription) {
        _workinDirectory = plugDescription.getWorkinDirectory();
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
                        LOG.fine("found search.done file: " + searchDone[0].getAbsolutePath());
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
            LOG.fine("found index-part: " + part.getAbsolutePath());
            indices.add(part);
        }
    }

}
