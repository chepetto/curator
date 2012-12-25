package org.curator.core.analysis;

import java.util.HashSet;
import java.util.Set;

public class TermFrequencyGlobal {
    private transient final String word;
    private transient Integer frequency = 0;
    private transient final Set<TermFrequencyDocument> documents = new HashSet<TermFrequencyDocument>(30);

    public TermFrequencyGlobal(final TermFrequencyDocument document, final Integer frequency) {
        this.word = document.getWord();
        this.frequency = frequency;
        documents.add(document);
    }

    public void addDocument(TermFrequencyDocument document) {
        documents.add(document);
        frequency += document.getFrequency();
    }

    public Integer getFrequency() {
        return frequency;
    }

    public String getWord() {
        return word;
    }

    public Set<TermFrequencyDocument> documents() {
        return documents;
    }
}
