package de.ingrid.iplug.se;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.LocalFileSystem;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiReader;

import de.ingrid.utils.PlugDescription;
import de.ingrid.utils.metadata.IPlugOperatorFinder;

public class IPlugSeOperatorFinder implements IPlugOperatorFinder {

  // private static final Logger LOG = Logger
  // .getLogger(IPlugSeOperatorFinder.class.getName());

  private FileSystem _fileSystem = null;

  private File _workinDirectory;

  public List<File> findIndices(File folder) throws IOException {
    _fileSystem = _fileSystem == null ? new LocalFileSystem() : _fileSystem;
    List<File> indices = new ArrayList<File>();

    List<File> allCrawls = new ArrayList<File>();

    // nutch instances
    File[] listFiles = folder.listFiles(new FileFilter() {
      @Override
      public boolean accept(File pathname) {
        return pathname.isDirectory();
      }
    });

    for (int i = 0; listFiles != null && i < listFiles.length; i++) {
      File file = listFiles[i];
      listFiles[i] = new File(file, "crawls");
    }
    // crawl folder
    if (listFiles != null) {
      for (File instance : listFiles) {
        File[] crawls = instance.listFiles(new FileFilter() {
          @Override
          public boolean accept(File pathname) {
            return pathname.isDirectory() && pathname.getName().startsWith("Crawl")
                && new File(pathname, "search.done").exists();
          }
        });
        if (crawls != null) {
          for (File crawl : crawls) {
            allCrawls.add(crawl);
          }
        }
      }
    }

    // indices
    for (File file : allCrawls) {
      File index = new File(file, "index");
      if (index.exists()) {
          indices.add(index);
      }
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
    Set<String> valueSet = new HashSet<String>();
    new IndexValueReader().pushValues(multiReader, indexFieldName, valueSet);
    multiReader.close();
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

}
