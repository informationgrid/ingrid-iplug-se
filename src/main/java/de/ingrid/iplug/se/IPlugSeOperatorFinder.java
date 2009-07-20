package de.ingrid.iplug.se;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.LocalFileSystem;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiReader;
import org.apache.lucene.store.FSDirectory;

import de.ingrid.utils.PlugDescription;
import de.ingrid.utils.metadata.IPlugOperatorFinder;

public class IPlugSeOperatorFinder implements IPlugOperatorFinder {

	private static class FinderRunnable implements Runnable {

		private final File _workingFolder;
		private final String _indexFieldName;
		private Set<String> _cache;
		private FileSystem _fileSystem = null;
		private final Object _mutex = new Object();
		private boolean _isRunning = false;

		public FinderRunnable(File workingFolder, String indexFieldName) {
			_workingFolder = workingFolder;
			_indexFieldName = indexFieldName;
			_cache = new HashSet<String>();
		}

		@Override
		public void run() {
			_isRunning = true;
			try {
				List<File> indices = findIndices(_workingFolder);
				IndexReader[] readers = new IndexReader[indices.size()];
				for (int i = 0; i < indices.size(); i++) {
					FSDirectory directory = FSDirectory.getDirectory(indices
							.get(i), false);
					readers[i] = IndexReader.open(directory);
				}
				MultiReader multiReader = new MultiReader(readers);
				Set<String> valueSet = new HashSet<String>();
				new IndexValueReader().pushValues(multiReader, _indexFieldName,
						valueSet);
				multiReader.close();
				valueSet.remove(null);

				synchronized (_mutex) {
					_cache.clear();
					_cache.addAll(valueSet);
				}

			} catch (Throwable e) {
				e.printStackTrace();
			} finally {
				_isRunning = false;
			}
		}

		public void setFileSystem(FileSystem fileSystem) {
			_fileSystem = fileSystem;
		}

		public FileSystem getFileSystem() {
			return _fileSystem;
		}

		public Set<String> getValues() {
			System.out
					.println("IPlugSeOperatorFinder.FinderRunnable.getValues() "
							+ _cache);
			Set<String> set = new HashSet<String>();
			synchronized (_mutex) {
				set.addAll(_cache);
			}
			System.out
					.println("IPlugSeOperatorFinder.FinderRunnable.getValues() "
							+ set);
			return set;
		}

		public boolean isRunning() {
			return _isRunning;
		}

		private List<File> findIndices(File folder) throws IOException {
			_fileSystem = _fileSystem == null ? new LocalFileSystem(
					new Configuration()) : _fileSystem;
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

		private void findAllFolders(final FileSystem fileSystem, File folder,
				List<File> list) {
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

		private void extractSegments(final FileSystem fileSystem, File folder,
				List<File> segmentList) throws IOException {
			File[] segments = fileSystem.listFiles(folder, (new FileFilter() {
				@Override
				public boolean accept(File pathname) {
					boolean ret = false;
					try {
						boolean isDirectory = fileSystem.isDirectory(pathname);
						File[] searchDone = new File[] {};
						if (isDirectory) {
							searchDone = fileSystem.listFiles(pathname,
									new FileFilter() {
										@Override
										public boolean accept(File pathname) {
											boolean ret = false;
											try {
												boolean isFile = fileSystem
														.isFile(pathname);
												boolean isSearchable = pathname
														.getName().equals(
																"search.done");
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
							LOG.fine("found search.done file: "
									+ searchDone[0].getAbsolutePath());
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

		private void extractIndices(final FileSystem fileSystem, File segment,
				List<File> indices) throws IOException {
			File index = new File(segment, "index");
			File[] parts = fileSystem.listFiles(index, new FileFilter() {
				@Override
				public boolean accept(File pathname) {
					boolean ret = false;
					try {
						ret = fileSystem.isDirectory(pathname)
								&& pathname.getName().startsWith("part");
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

	private static final Logger LOG = Logger
			.getLogger(IPlugSeOperatorFinder.class.getName());

	private FinderRunnable _partnerFinder;

	private FinderRunnable _providerFinder;

	public void setFileSystem(FileSystem fileSystem) {
		_partnerFinder.setFileSystem(fileSystem);
		_providerFinder.setFileSystem(fileSystem);
	}

	@Override
	public Set<String> findPartner() throws IOException {
		if (!_partnerFinder.isRunning()) {
			Thread thread = new Thread(_partnerFinder);
			thread.setDaemon(true);
			thread.start();
		}
		return _partnerFinder.getValues();
	}

	@Override
	public Set<String> findProvider() throws IOException {
		if (!_providerFinder.isRunning()) {
			Thread thread = new Thread(_providerFinder);
			thread.setDaemon(true);
			thread.start();
		}

		return _providerFinder.getValues();
	}

	@Override
	public void configure(PlugDescription plugDescription) {
		FSDirectory.setDisableLocks(false);
		File workinDirectory = plugDescription.getWorkinDirectory();
		_partnerFinder = new FinderRunnable(workinDirectory, "partner");
		_providerFinder = new FinderRunnable(workinDirectory, "provider");

		// start partner searching
		Thread thread = new Thread(_partnerFinder);
		thread.setDaemon(true);
		thread.start();

		// start provider searching
		Thread thread2 = new Thread(_providerFinder);
		thread2.setDaemon(true);
		thread2.start();
	}

}
