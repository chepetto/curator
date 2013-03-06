package org.curator.core.analysis;

import org.curator.core.model.Article;

public class TermFrequencyDocument {
    private Article article;
    private int frequency;
    private String word;
    private int termsCount;

    public TermFrequencyDocument(Article article, String word, int frequency, int termsCount) {
        this.article = article;
        this.frequency = frequency;
        this.word = word;
        this.termsCount = termsCount;
    }

    public Article getArticle() {
        return article;
    }

    public int getFrequency() {
        return frequency;
    }

    public String getWord() {
        return word;
    }

    public int getTermsCount() {
        return termsCount;
    }

}
