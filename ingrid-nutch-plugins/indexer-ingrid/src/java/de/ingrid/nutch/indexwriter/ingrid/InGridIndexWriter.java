/**
 * 
 */
package de.ingrid.nutch.indexwriter.ingrid;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import org.apache.commons.lang.NotImplementedException;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapred.FileOutputFormat;
import org.apache.hadoop.mapred.JobConf;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriter.MaxFieldLength;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.FSDirectory;
import org.apache.nutch.indexer.NutchDocument;
import org.apache.nutch.indexer.NutchField;
import org.apache.nutch.metadata.Metadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.ingrid.nutch.analysis.AnalyzerFactory;
import de.ingrid.nutch.analysis.NutchAnalyzer;
import de.ingrid.nutch.analysis.NutchDocumentAnalyzer;
import de.ingrid.nutch.indexer.Indexer;
import de.ingrid.nutch.indexer.IndexerConstants;
import de.ingrid.nutch.indexer.NutchSimilarity;
import de.ingrid.nutch.indexer.lucene.LuceneConstants;
import de.ingrid.nutch.indexer.lucene.LuceneWriter;
import de.ingrid.nutch.util.LogUtil;

/**
 * Writes the Ingrid specific local index.
 * 
 * @author joachim
 * 
 */
public class InGridIndexWriter implements org.apache.nutch.indexer.IndexWriter {

    public static final Logger LOG = LoggerFactory.getLogger(InGridIndexWriter.class);
    

    public static enum STORE {
        YES, NO, COMPRESS
    }

    public static enum INDEX {
        NO, NO_NORMS, TOKENIZED, UNTOKENIZED
    }

    public static enum VECTOR {
        NO, OFFSET, POS, POS_OFFSET, YES
    }

    private IndexWriter writer;

    private AnalyzerFactory analyzerFactory;

    private Path perm;

    private Path temp;

    private FileSystem fs;

    private final Map<String, Field.Store> fieldStore;

    private final Map<String, Field.Index> fieldIndex;

    private final Map<String, Field.TermVector> fieldVector;
    
    private Configuration config;


    public InGridIndexWriter() {
        fieldStore = new HashMap<String, Field.Store>();
        fieldIndex = new HashMap<String, Field.Index>();
        fieldVector = new HashMap<String, Field.TermVector>();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.hadoop.conf.Configurable#getConf()
     */
    @Override
    public Configuration getConf() {
        return config;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.hadoop.conf.Configurable#setConf(org.apache.hadoop.conf.
     * Configuration)
     */
    @Override
    public void setConf(Configuration conf) {
        config = conf;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.nutch.indexer.IndexWriter#close()
     */
    @Override
    public void close() throws IOException {
        writer.optimize();
        writer.close();
        fs.completeLocalOutput(perm, temp); // copy to dfs
        fs.createNewFile(new Path(perm, IndexerConstants.DONE_NAME));
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.nutch.indexer.IndexWriter#commit()
     */
    @Override
    public void commit() throws IOException {
        throw new NotImplementedException();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.nutch.indexer.IndexWriter#delete(java.lang.String)
     */
    @Override
    public void delete(String key) throws IOException {
        writer.deleteDocuments(new Term("url", key));
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.nutch.indexer.IndexWriter#describe()
     */
    @Override
    // TODO describe available options
    public String describe() {
        StringBuffer sb = new StringBuffer("InGridIndexWriter\n");
        sb.append("\t").append("ingridindexwriter.path").append(" : Path of the temporary index (TOBE FIXED)\n");
        return sb.toString();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.apache.nutch.indexer.IndexWriter#open(org.apache.hadoop.mapred.JobConf
     * , java.lang.String)
     */
    @Override
    public void open(JobConf job, String name) throws IOException {
        this.fs = FileSystem.get(job);
        perm = new Path(FileOutputFormat.getOutputPath(job), name);
        temp = job.getLocalPath("index/_"  +
                          Integer.toString(new Random().nextInt()));

        fs.delete(perm, true); // delete old, if any
        analyzerFactory = new AnalyzerFactory(job);
        writer = new IndexWriter(
            FSDirectory.open(new File(fs.startLocalOutput(perm, temp).toString())),
            new NutchDocumentAnalyzer(job), true, MaxFieldLength.UNLIMITED);

        writer.setMergeFactor(job.getInt("indexer.mergeFactor", 10));
        writer.setMaxBufferedDocs(job.getInt("indexer.minMergeDocs", 100));
        writer.setMaxMergeDocs(job
            .getInt("indexer.maxMergeDocs", Integer.MAX_VALUE));
        writer.setTermIndexInterval(job.getInt("indexer.termIndexInterval", 128));
        writer.setMaxFieldLength(job.getInt("indexer.max.tokens", 10000));
        writer.setInfoStream(LogUtil.getDebugStream(Indexer.LOG));
        writer.setUseCompoundFile(false);
        writer.setSimilarity(new NutchSimilarity());

        processOptions(job);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.apache.nutch.indexer.IndexWriter#update(org.apache.nutch.indexer.
     * NutchDocument)
     */
    @Override
    public void update(NutchDocument doc) throws IOException {
        write(doc);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.nutch.indexer.IndexWriter#write(org.apache.nutch.indexer.
     * NutchDocument)
     */
    @Override
    public void write(NutchDocument doc) throws IOException {
        final Document luceneDoc = createLuceneDoc(doc);
        final NutchAnalyzer analyzer = analyzerFactory.get(luceneDoc.get("lang"));
        if (LOG.isDebugEnabled()) {
          LOG.debug("Indexing [" + luceneDoc.get("url")
              + "] with analyzer " + analyzer + " (" + luceneDoc.get("lang")
              + ")");
        }
        writer.addDocument(luceneDoc, analyzer);
    }
    
    private Document createLuceneDoc(NutchDocument doc) {
        final Document out = new Document();

        out.setBoost(doc.getWeight());

        final Metadata documentMeta = doc.getDocumentMeta();
        for (final Entry<String, NutchField> entry : doc) {
          final String fieldName = entry.getKey();

          Field.Store store = fieldStore.get(fieldName);
          Field.Index index = fieldIndex.get(fieldName);
          Field.TermVector vector = fieldVector.get(fieldName);

          // default values
          if (store == null) {
            store = Field.Store.NO;
          }

          if (index == null) {
            index = Field.Index.NO;
          }

          if (vector == null) {
            vector = Field.TermVector.NO;
          }

          // read document-level field information
          final String[] fieldMetas =
            documentMeta.getValues(LuceneConstants.FIELD_PREFIX + fieldName);
          if (fieldMetas.length != 0) {
            for (final String val : fieldMetas) {
              if (LuceneConstants.STORE_YES.equals(val)) {
                store = Field.Store.YES;
              } else if (LuceneConstants.STORE_NO.equals(val)) {
                store = Field.Store.NO;
              } else if (LuceneConstants.INDEX_TOKENIZED.equals(val)) {
                index = Field.Index.ANALYZED;
              } else if (LuceneConstants.INDEX_NO.equals(val)) {
                index = Field.Index.NO;
              } else if (LuceneConstants.INDEX_UNTOKENIZED.equals(val)) {
                index = Field.Index.NOT_ANALYZED;
              } else if (LuceneConstants.INDEX_NO_NORMS.equals(val)) {
                index = Field.Index.ANALYZED_NO_NORMS;
              } else if (LuceneConstants.VECTOR_NO.equals(val)) {
                vector = Field.TermVector.NO;
              } else if (LuceneConstants.VECTOR_YES.equals(val)) {
                vector = Field.TermVector.YES;
              } else if (LuceneConstants.VECTOR_POS.equals(val)) {
                vector = Field.TermVector.WITH_POSITIONS;
              } else if (LuceneConstants.VECTOR_POS_OFFSET.equals(val)) {
                vector = Field.TermVector.WITH_POSITIONS_OFFSETS;
              } else if (LuceneConstants.VECTOR_OFFSET.equals(val)) {
                vector = Field.TermVector.WITH_OFFSETS;
              }
            }
          }

          for (final Object fieldValue : entry.getValue().getValues()) {
            Field f = new Field(fieldName, fieldValue.toString(), store, index, vector);
            f.setBoost(entry.getValue().getWeight());
            out.add(f);
          }
        }

        return out;
      }

    private void processOptions(Configuration conf) {
        @SuppressWarnings("rawtypes")
        final Iterator iterator = conf.iterator();
        while (iterator.hasNext()) {
            @SuppressWarnings("rawtypes")
            final String key = (String) ((Map.Entry) iterator.next()).getKey();
            if (!key.startsWith(LuceneConstants.LUCENE_PREFIX)) {
                continue;
            }
            if (key.startsWith(LuceneConstants.FIELD_STORE_PREFIX)) {
                final String field = key.substring(LuceneConstants.FIELD_STORE_PREFIX.length());
                final LuceneWriter.STORE store = LuceneWriter.STORE.valueOf(conf.get(key));
                switch (store) {
                case YES:
                case COMPRESS:
                    fieldStore.put(field, Field.Store.YES);
                    break;
                case NO:
                    fieldStore.put(field, Field.Store.NO);
                    break;
                }
            } else if (key.startsWith(LuceneConstants.FIELD_INDEX_PREFIX)) {
                final String field = key.substring(LuceneConstants.FIELD_INDEX_PREFIX.length());
                final LuceneWriter.INDEX index = LuceneWriter.INDEX.valueOf(conf.get(key));
                switch (index) {
                case NO:
                    fieldIndex.put(field, Field.Index.NO);
                    break;
                case NO_NORMS:
                    fieldIndex.put(field, Field.Index.NOT_ANALYZED_NO_NORMS);
                    break;
                case TOKENIZED:
                    fieldIndex.put(field, Field.Index.ANALYZED);
                    break;
                case UNTOKENIZED:
                    fieldIndex.put(field, Field.Index.NOT_ANALYZED);
                    break;
                }
            } else if (key.startsWith(LuceneConstants.FIELD_VECTOR_PREFIX)) {
                final String field = key.substring(LuceneConstants.FIELD_VECTOR_PREFIX.length());
                final LuceneWriter.VECTOR vector = LuceneWriter.VECTOR.valueOf(conf.get(key));
                switch (vector) {
                case NO:
                    fieldVector.put(field, Field.TermVector.NO);
                    break;
                case OFFSET:
                    fieldVector.put(field, Field.TermVector.WITH_OFFSETS);
                    break;
                case POS:
                    fieldVector.put(field, Field.TermVector.WITH_POSITIONS);
                    break;
                case POS_OFFSET:
                    fieldVector.put(field, Field.TermVector.WITH_POSITIONS_OFFSETS);
                    break;
                case YES:
                    fieldVector.put(field, Field.TermVector.YES);
                    break;
                }
            }
        }
    }

}
