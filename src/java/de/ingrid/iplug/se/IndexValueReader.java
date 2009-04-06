package de.ingrid.iplug.se;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermEnum;

public class IndexValueReader {

    public void pushValues(IndexReader reader, String fieldName, Set<String> set) throws IOException {
        TermEnum terms = reader.terms(new Term(fieldName, ""));
        while (terms.next()) {
            Term term = terms.term();
            if (term.field().equals(fieldName)) {
                if (term.text() != null) {
                    set.add(term.text());
                }
            }

        }
    }

    public static void main(String[] args) throws IOException {
        IndexReader open = IndexReader.open(new File("/Users/mb/Desktop/20090311115955/index/part-00000"));
        Set<String> set = new HashSet<String>();
        new IndexValueReader().pushValues(open, "provider", set);
        System.out.println(set);

    }

}
