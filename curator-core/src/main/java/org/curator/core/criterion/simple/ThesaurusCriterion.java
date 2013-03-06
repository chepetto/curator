package org.curator.core.criterion.simple;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.curator.common.exceptions.CuratorException;
import org.curator.common.model.Sentence;
import org.curator.common.model.Word;
import org.curator.core.criterion.AbstractSimpleCriterion;
import org.curator.core.criterion.Goal;
import org.curator.core.criterion.Performance;
import org.curator.core.criterion.SinglePerformance;
import org.curator.core.model.Article;
import org.curator.core.model.MetricName;
import org.curator.core.model.MetricProvider;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Wortschatz analyse
 */
@LocalBean
@Stateless
public class ThesaurusCriterion extends AbstractSimpleCriterion implements MetricProvider {

    private static final Logger LOGGER = Logger.getLogger(ThesaurusCriterion.class);

    private static final GermanStemmer stemmer = new GermanStemmer();

    @Override
    public Performance evalCriterion(final Article source, final Goal goal) throws CuratorException {

        // -- ASSERTIONS -- --------------------------------------------------------------------------------------------
        if (source == null) {
            throw new IllegalArgumentException("source is null");
        }
        if (!source.hasMetricResult(MetricName.TYPE_TOKEN_RATIO)) {
            LOGGER.warn("Too few metric results: " + MetricName.TYPE_TOKEN_RATIO);
            return null;
        }
        // -- ----------------------------------------------------------------------------------------------------------

        double ttr = source.getMetricResult(MetricName.TYPE_TOKEN_RATIO);

        double performance;
        if (Goal.MODEST_TEXT == getGoal()) {
            performance = 1 - ttr;
        } else {
            performance = ttr;
        }

        return new SinglePerformance(this, performance);
    }

    @Override
    public void pushMetricResults(Article article) throws CuratorException {
        if (article.getContent() == null) {
            return;
        }
        double ttr = getTypeTokenRatio(article);
        article.addMetricResult(MetricName.TYPE_TOKEN_RATIO, ttr);
        LOGGER.trace("eval " + MetricName.TYPE_TOKEN_RATIO + ": " + ttr);
    }

    /**
     * Wortschatz
     * http://de.wikipedia.org/wiki/Type-Token-Relation
     * Hoch, wenn ein großer Wortschatz verwendet wird
     */
    private double getTypeTokenRatio(Article source) {

        final Map<String, Set<String>> stemmedMap = new HashMap<String, Set<String>>(2000);

        for (Sentence s : source.getContent().getSentences()) {
            for (Word w : s.getWords()) {
                try {
                    String raw = w.getValue();
                    if (StringUtils.isBlank(raw)) {
                        continue;
                    }
                    String stemmed = stemmer.stem(raw);

                    if (stemmed.length() == 0) continue;
                    if (!stemmedMap.containsKey(stemmed)) {
                        stemmedMap.put(stemmed, new HashSet<String>(3));
                    }
                    stemmedMap.get(stemmed).add(raw);
                } catch (Exception e) {
                    LOGGER.error("Cannot stem '" + w.getValue() + "': " + e.getMessage());
                }
            }
        }

        int typeCount = stemmedMap.size();
        int tokenCount = 0;
        for (String w : stemmedMap.keySet()) {
            tokenCount += stemmedMap.get(w).size();
        }

        return typeCount / (double) Math.max(1, tokenCount);
    }

    @Override
    public String name() {
        return "ThesaurusCriterion";
    }
}
