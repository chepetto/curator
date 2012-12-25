package org.curator.core.analysis;

import java.util.*;

import org.apache.log4j.Logger;

import org.curator.common.model.Article;

public class KeywordAnalyzer implements Analyzer {

    private Logger LOGGER = Logger.getLogger(KeywordAnalyzer.class);

    private static final String DELIMITER = "?!,;:-–_#`´'”\".+*=<>()[]{}/\\| \t\n„“%";

    private static final String[] STOPWORDS = new String[]{"die", "und", "der", "das", "es", "in", "ist", "zu", "den", "von", "ein", "auf",
            "nicht", "ich", "auch", "eine", "mit", "das", "als", "aber", "oder", "sich", "dass", "sie", "man", "dem", "im", "des", "wie", "noch",
            "aus", "so", "an", "bei", "was", "für", "nur", "er", "wir", "wenn", "vor", "sind", "denn", "werden", "wird", "einer", "um", "hat",
            "einen", "mehr", "diese", "einem", "nach", "über", "zur", "dann", "zum", "haben", "kann", "durch", "da", "am", "keine", "doch",
            "war", "uns", "bis", "vom", "ihr", "ihre", "ihres", "ihrer", "immer", "sein", "seine", "schon", "gibt", "wurde", "habe", "ja",
            "weil", "geht", "also", "dieser", "viel", "mir", "du", "viele", "jetzt", "wieder", "damit", "unter", "sehr", "muss", "mich", "hab",
            "alle", "waren", "ob", "dazu", "gar", "oft", "würde", "könnte", "diesem", "erst", "weniger", "sondern", "bin", "the", "this", "wäre",
            "wer", "de", "nichts", "während", "kommen", "at", "heißt", "sagen", "seit", "eines", "daran", "wo", "sieht", "dieses", "große",
            "stehen", "brauchen", "halten", "soll", "letzten", "mal", "macht", "sowie", "würden", "sonst", "wirklich", "weiß", "findet",
            "erreichen", "andere", "einmal", "lesen", "kein", "ab", "nämlich", "kein", "ab", "dabei", "warum", "sagt", "beim", "groß", "leisten",
            "darf", "nachdem", "liegt", "wenig", "sowieso", "kaum", "kleinen", "fand", "unsere", "eher", "bereits", "alles", "möglich", "statt",
            "eigentlich", "tatsächlich", "passiert", "zwischen", "wollen", "davon", "eigenen", "deiner", "lauf", "dürften"};
    private Set<String> stopwordSet;

    public KeywordAnalyzer() {
        stopwordSet = new HashSet<String>(STOPWORDS.length);
        stopwordSet.addAll(Arrays.asList(STOPWORDS));
    }

    private final Map<String, TermFrequencyGlobal> wordFreqMap = new HashMap<String, TermFrequencyGlobal>(3000);
    private double documentCount = 0d;

    @Override
    public void analyze(Article article) {
        documentCount++;
        keywordsFreqAnalysis(article, article.getContent() + " " + article.getTitle());
        // freqWords.addAll(wordFreqMap.keySet());

        // System.out.println("----------");
        // int i = 0;
        // for (String w : freqWords) {
        // System.out.println(w + " " + wordFreqMap.get(w));
        // if (i++ > 300)
        // break;
        // }
    }

    private void keywordsFreqAnalysis(Article article, String text) {

        Map<String, Integer> docWordFreqMap = new HashMap<String, Integer>(100);

        StringTokenizer tokenizer = new StringTokenizer(text, DELIMITER);
        final int terms = tokenizer.countTokens();

        try {
            while (tokenizer.hasMoreTokens()) {
                final String token = tokenizer.nextToken().toLowerCase();

                if (token.length() < 2 || stopwordSet.contains(token)) {
                    continue;
                }

                int f = 1;
                if (docWordFreqMap.containsKey(token)) {
                    f = docWordFreqMap.get(token) + 1;
                }
                docWordFreqMap.put(token, f);
            }
        } catch (NoSuchElementException e) {
            LOGGER.error(e.getMessage());
            LOGGER.debug(e);
        }


        for (String word : docWordFreqMap.keySet()) {

            final int freq = docWordFreqMap.get(word);

            if (wordFreqMap.containsKey(word)) {
                TermFrequencyGlobal group = wordFreqMap.get(word);
                group.addDocument(new TermFrequencyDocument(article, word, freq, terms));

            } else {
                wordFreqMap.put(word, new TermFrequencyGlobal(new TermFrequencyDocument(article, word, freq, terms), freq));
            }
        }


    }

    public void teardown() {

//  calc TF-IDF
//        final Map<String, TermFrequencyUtils> map = new HashMap<String, TermFrequencyUtils>(wordFreqMap.size());
//
//        List<String> keys = new LinkedList<String>();
//        keys.addAll(wordFreqMap.keySet());
//
//        ListIterator<String> iterator = keys.listIterator();
//        while (iterator.hasNext()) {
//            String word = iterator.next();
//            TermFrequencyGlobal tfobj = wordFreqMap.get(word);
//
//            TermFrequencyUtils idf = new TermFrequencyUtils();
//            idf.setWord(word);
//            int termInDocumentsCount = tfobj.documents().size();
////      idf.setIdf(documentCount / termInDocumentsCount);
//
//            double avdTf = 0;
//            for (TermFrequencyDocument doc : tfobj.documents()) {
//                avdTf += (double) doc.getFrequency() / doc.getTermsCount();
//            }
//            idf.setIdf((documentCount / termInDocumentsCount) * (avdTf / tfobj.documents().size()));
//
//            map.put(word, idf);
//
//            iterator.remove();
//            wordFreqMap.remove(word);
//        }
//
//        final SortedSet<String> freqWords = new TreeSet<String>(new Comparator<String>() {
//            @Override
//            public int compare(String o1, String o2) {
//
//                Double f1 = map.get(o1).getIdf();
//                Double f2 = map.get(o2).getIdf();
//
//                if (f2.compareTo(f1) != 0) {
//                    return f1.compareTo(f2);
//                }
//                return -1;
//            }
//
//        });
//
//        freqWords.addAll(map.keySet());
//
//        System.out.println("----------");
//        int i = 0;
//        for (String w : freqWords) {
//            TermFrequencyUtils group = map.get(w);
//            System.out.printf("%s %s\n", w, group.getIdf());
//            if (i++ > 1500)
//                break;
//        }
    }

}
