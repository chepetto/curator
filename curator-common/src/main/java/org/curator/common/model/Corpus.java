package org.curator.common.model;

import org.curator.common.utils.TextUtils;

import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

public abstract class Corpus {

    private static final String PUNCTUACTIONS = " -+:()[]{}<>/\\&%$|\"';,.!?";

    private List<Sentence> sentenceList;
    private int wordCount = -1;

    public List<Sentence> getSentences() {
        return sentenceList;
    }

    public int getWordCount() {
        if(wordCount==-1) {
            wordCount = 0;
            for(Sentence s : getSentences()) {
                wordCount += s.getWords().size();
            }
        }
        return wordCount;
    }

    public void setText(String content) {
        sentenceList = new LinkedList<Sentence>();

        StringTokenizer ts = new StringTokenizer(content, ";.!?");
        while (ts.hasMoreTokens()) {

            String sentence = ts.nextToken();
            if(sentence.length()<3) {
                continue;
            }
            Sentence s = new Sentence(sentence);

            StringTokenizer tw = new StringTokenizer(sentence, PUNCTUACTIONS);
            while(tw.hasMoreTokens()) {

                Word w = new Word(TextUtils.trim(tw.nextToken()));
                s.add(w);
            }

            sentenceList.add(s);

        }
    }
}
