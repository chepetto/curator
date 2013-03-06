package org.curator.core.criterion.simple;

import com.itextpdf.text.pdf.hyphenation.Hyphenation;
import com.itextpdf.text.pdf.hyphenation.Hyphenator;
import org.apache.log4j.Logger;
import org.curator.common.model.Content;
import org.curator.common.model.Corpus;
import org.curator.common.model.Sentence;
import org.curator.common.model.Word;
import org.curator.core.criterion.AbstractSimpleCriterion;
import org.curator.core.criterion.Goal;
import org.curator.core.criterion.MultiplePerformance;
import org.curator.core.model.MetricName;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

public abstract class AbstractReadingEaseCriterion extends AbstractSimpleCriterion {

    private static final transient Logger LOGGER = Logger.getLogger(AbstractReadingEaseCriterion.class);
    private final Pattern url = Pattern.compile("http[s]?://[a-zA-Z0-9-.]+\\.[a-zA-Z]{2,3}(\\S*)?[^ ]*");

    private static final String PUNCTUACTIONS = " -+:()[]{}/&%$';,.!?";

    // http://de.wikipedia.org/wiki/Liste_lateinischer_Suffixe
    // http://de.wikipedia.org/wiki/Liste_griechischer_Suffixe
    private static final String[] POST_LATIN = {"an", "ar", "at", "ium", "men", "ion", "or", "ur", "us", "itieren", "eszieren", "zid", "nt", "nd", "trix"};
    private static final String[] POST_GREEK = {"ie", "ia​", "ik", "ist", "st", "istisch", "stisch", "istik", "stik", "itis", "ma", "mata", "men", "me", "oeides", "on", "sis", "se", "plo"};
    // http://de.wikipedia.org/wiki/Liste_lateinischer_Pr%C3%A4fixe
    // http://de.wikipedia.org/wiki/Liste_griechischer_Pr%C3%A4fixe
    private static final String[] PRE_LATIN = {"ambi", "ante", "aequi", "bi", "circum", "cis", "con", "contra", "dextro", "dis", "ex", "extra", "infra", "inter", "intra", "iuxta", "laev", "multi", "non", "omni", "per", "por", "post", "prae", "prä", "pro", "re", "semi", "super", "supra", "trans", "ultra"};
    private static final String[] PRE_GREEK = {"all", "allo", "amphi", "ana", "an", "angio", "angi", "anti", "ant", "apo", "ap", "auto", "dia", "dys", "ek", "ex", "eu", "ev", "exo", "halo", "hemi", "hetero", "holo", "homo", "homöo", "hyper", "hypo", "iso", "kata", "kat", "krypto", "lipos", "mega", "meta", "met", "mikro", "ortho", "pan", "para", "peri", "poly", "pro", "pros", "pseudo", "syn", "sy", "syl", "sym", "tele"};

    private static final List<String> PREFIX = new LinkedList<String>() {{
        addAll(Arrays.asList(PRE_GREEK));
        addAll(Arrays.asList(PRE_LATIN));
    }};

    private static final List<String> POSTFIX = new LinkedList<String>() {{
        addAll(Arrays.asList(POST_GREEK));
        addAll(Arrays.asList(POST_LATIN));
    }};

    public AbstractReadingEaseCriterion() {
        //
    }

    protected double getRelativeBestPerformance(double bestResult, double worstResult) {
        double max = Math.max(bestResult, worstResult);
        double min = Math.min(bestResult, worstResult);

        double delta = -min;

        max += delta;
        min += delta;

        return (bestResult > worstResult ? max : min) / 100d;
    }

    protected double getRelativePerformance(double result, double bestResult, double worstResult, MetricName metricName) {

        double max = Math.max(bestResult, worstResult);
        double min = Math.min(bestResult, worstResult);

        double delta = -min;

        max += delta;
        min += delta;
        result += delta;

        double performance;
        if (result > max) {
            performance = 1;
        } else if (result < min) {
            performance = 0;
        } else {
            performance = result / max;
        }

        return bestResult > worstResult ? performance : 1 - performance;

    }


    // -- AMDAHL -- ----------------------------------------------------------------------------------------------------

    /**
     * Dieser Index gilt für alle Textsorten und ist daher unproblematisch in der Anwendung.
     * Die Resultate liegen zwischen x und y, damit gilt: je höher der Wert, desto verständlicher ist der Text.
     *
     * @param locale
     * @param corpus
     * @return
     */
    protected double getAmdahlIndex(Locale locale, Corpus corpus) {
        double dwortzahl = corpus.getWordCount();
        double drec = corpus.getSentences().size();
        double dsizages = getTotalNumberOfSyllables(locale, corpus);
        return 180.0 - ((dwortzahl / Math.max(1, drec)) + ((dsizages / Math.max(1, dwortzahl)) * 58.5));
    }

    private double getAbsWorstPerformance(double max, double min, Goal goal) {
        if (Goal.MODEST_TEXT == goal) {
            return max;
        } else {
            return min;
        }
    }

    private double getAbsBestPerformance(double max, double min, Goal goal) {
        if (Goal.MODEST_TEXT == goal) {
            return min;
        } else {
            return max;
        }
    }

    // -- FLESCH -- ----------------------------------------------------------------------------------------------------

    /**
     * Flesch Reading Ease
     * Je höher ihr Wert ist, desto leichter verständlich ist der Text
     *
     * @return
     */
    protected double getFleschIndex(Locale locale, Corpus corpus) {
        return 206.835 - 1.015 * getAvgLengthOfSentence(corpus) - 84.6 * getAvgNumberOfSyllables(locale, corpus);
    }

    protected void addMetricResult(MultiplePerformance result, MetricName metricName, double absoluteScore, double min, double max) {

        double relBestPerformance = getRelativeBestPerformance(max, min);

        double relPerformance = getRelativePerformance(absoluteScore, getAbsBestPerformance(max, min, getGoal()), getAbsWorstPerformance(max, min, getGoal()), metricName);

        result.addResult(metricName, relBestPerformance, relPerformance);
    }

    // -- SMOG -- ------------------------------------------------------------------------------------------------------

    protected static double getSMOGIndex(Locale locale, Corpus corpus) {
        if (Locale.ENGLISH.equals(locale)) {
            // http://en.wikipedia.org/wiki/SMOG_(Simple_Measure_Of_Gobbledygook)
            return 1.043 * Math.sqrt(getPolySyllablesCount(locale, corpus) * 30 / corpus.getSentences().size()) + 3.1291;
        } else {
            // Deutscher SMOG-Index von Bamberger
            return Math.sqrt(getPolySyllablesCount(locale, corpus)) - 2;
        }
    }

    /**
     * Total number of words with three ore more one hypen
     */
    private static double getPolySyllablesCount(Locale locale, Corpus content) {
        Hyphenator h = new Hyphenator(locale.getLanguage(), null, 1, 1);
        int total3OrMoreHyphenCount = 0;

        for (Sentence s : content.getSentences()) {
            for (Word w : s.getWords()) {
                Hyphenation hy = h.hyphenate(w.getValue());
                total3OrMoreHyphenCount += hy != null && hy.length() >= 3 ? 1 : 0;
            }
        }
        return total3OrMoreHyphenCount;
    }


    // Average Number of Syllables per Word
    public double getAvgNumberOfSyllables(final Locale locale, Corpus corpus) {
        double wordCount = (double) corpus.getWordCount();
        return getTotalNumberOfSyllables(locale, corpus) / Math.max(1, wordCount);
    }

    // Average Sentence Length
    public double getAvgLengthOfSentence(Corpus content) {
        double sentenceCount = (double) content.getSentences().size();
        return content.getWordCount() / Math.max(1, sentenceCount);
    }

    // Total number of Syllables
    public double getTotalNumberOfSyllables(Locale locale, Corpus corpus) {

        Hyphenator h = new Hyphenator(locale.getLanguage(), null, 1, 1);
        int totalHyphenCount = 0;

        for (Sentence s : corpus.getSentences()) {
            for (Word w : s.getWords()) {
                Hyphenation hy = h.hyphenate(w.getValue());
                totalHyphenCount += hy == null ? 1 : hy.length() + 1;
            }
        }
        return totalHyphenCount;
    }


    /**
     * Average number of loan words in a sentence
     */
    public double getAvgNumberOfLoadWords(Content content) {

        double totalAvgLoanwords = 0;

        for (Sentence s : content.getSentences()) {
            int loanwordCount = 0;
            for (Word w : s.getWords()) {
                String v = w.getValue().toLowerCase();
                for (String _pre : PREFIX) {
                    if (v.startsWith(_pre) && v.length() > _pre.length()) {
                        loanwordCount++;
                        break;
                    }
                }

                for (String _post : POSTFIX) {
                    if (v.endsWith(_post) && v.length() > _post.length()) {
                        loanwordCount++;
                        break;
                    }
                }
            }

            if (loanwordCount > 0) {
                totalAvgLoanwords += s.getWords().size() / (double) loanwordCount;
            }
        }


        return totalAvgLoanwords / content.getSentences().size();
    }

    /**
     * average number of punctuation marks
     */
    public double getAvgNumberOfPunctuations(Content content) {

        double total = 0;
        for (Sentence s : content.getSentences()) {
            total += countOccurrences(s.getText(), PUNCTUACTIONS);
        }

        return total / content.getSentences().size();
    }

    public static int countOccurrences(String haystack, String needles) {
        int count = 0;
        for (int i = 0; i < haystack.length(); i++) {
            if (needles.indexOf(haystack.charAt(i)) > -1) {
                count++;
            }
        }
        return count;
    }


    private String escapeUrls(String text) {
        return url.matcher(text).replaceAll("url");
    }

}
