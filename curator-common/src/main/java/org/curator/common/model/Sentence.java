package org.curator.common.model;

import java.util.LinkedList;
import java.util.List;

public class Sentence {
    private List<Word> wordList = new LinkedList<Word>();
    private String text;

    public Sentence(String text) {
        this.text = text;
    }

    public void add(Word w) {
        wordList.add(w);
    }

    public List<Word> getWords() {
        return wordList;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

}
