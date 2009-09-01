package de.ingrid.iplug.se;

import java.io.IOException;
import java.util.Set;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermEnum;

public class IndexValueReader {

    public void pushValues(IndexReader reader, String fieldName, Set<String> set) throws IOException {
        TermEnum terms = reader.terms();
        while (terms.next()) {
            Term term = terms.term();
            if (term.field().equals(fieldName)) {
                if (term.text() != null) {
                    set.add(term.text());
                }
            }

        }
    }


}
