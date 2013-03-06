package org.curator.core.criterion.simple;

import org.apache.log4j.Logger;
import org.curator.common.configuration.Configuration;
import org.curator.common.exceptions.CuratorException;
import org.curator.common.model.Comment;
import org.curator.core.criterion.Goal;
import org.curator.core.criterion.Performance;
import org.curator.core.criterion.SinglePerformance;
import org.curator.core.model.Article;
import org.curator.core.model.MetricName;
import org.curator.core.model.MetricProvider;

import java.util.*;

public class CommentsFreshnessCriterion extends AbstractFreshnessCriterion implements MetricProvider {

    private static final Logger LOGGER = Logger.getLogger(CommentCountCriterion.class);

    private static final int HISTOGRAM_COLUMNS = 10;

    @Override
    public Performance evalCriterion(final Article source, final Goal goal) throws CuratorException {

        // -- ASSERTIONS -- --------------------------------------------------------------------------------------------
        if (source == null) {
            throw new IllegalArgumentException("source is null");
        }
        if (source.getComments() == null) {
            return null;
        }
        if (!source.hasMetricResult(MetricName.COMMENT_FRESHNESS)) {
            LOGGER.warn("Too few metric results: " + MetricName.COMMENT_FRESHNESS);
            return null;
        }
        // -- ----------------------------------------------------------------------------------------------------------

        double result = source.getMetricResult(MetricName.COMMENT_FRESHNESS);

        LOGGER.trace("eval " + name() + ": " + result);
        return new SinglePerformance(this, result);
    }

    @Override
    public void pushMetricResults(Article article) throws CuratorException {
        double freshness = getFreshness(findGravityCenterOfDates(extractDates(article)));
        article.addMetricResult(MetricName.COMMENT_FRESHNESS, freshness);
        LOGGER.trace("eval " + MetricName.COMMENT_FRESHNESS + ": " + freshness);
    }


    protected Date findGravityCenterOfDates(final List<Calendar> dates) {
        // histogram
        final long expiry = Configuration.getFreshnessExpiry().getTimeInMillis();
        final long maxDiff = new Date().getTime() - expiry;

        final Map<Integer, Calendar> histogramLimits = getHistogramLimits(HISTOGRAM_COLUMNS, expiry, maxDiff / HISTOGRAM_COLUMNS);
        final Map<Integer, Integer> histogramFreq = initHistogramFreq(HISTOGRAM_COLUMNS);

        for (final Calendar date : dates) {

            for (int index = 0; index < HISTOGRAM_COLUMNS; index++) {
                if (date.before(histogramLimits.get(index))) {
                    histogramFreq.put(index, histogramFreq.get(index) + 1);
                    break;
                }

            }
        }

        // weight, with x^2

        // find max
        int maxFreq = 0;
        int dominantIndex = 0;
        for (int i = 0; i < HISTOGRAM_COLUMNS; i++) {
            if (maxFreq <= histogramFreq.get(i)) {
                maxFreq = histogramFreq.get(i);
                dominantIndex = i;
            }
        }

        return histogramLimits.get(dominantIndex).getTime();
    }

    private Map<Integer, Integer> initHistogramFreq(int columns) {
        Map<Integer, Integer> freq = new HashMap<Integer, Integer>(columns);
        for (int i = 0; i < columns; i++) {
            freq.put(i, 0);
        }
        return freq;
    }

    private Map<Integer, Calendar> getHistogramLimits(int columns, long expiryMillis, double stepMillis) {
        Map<Integer, Calendar> histogram = new HashMap<Integer, Calendar>(columns);

        for (int i = 0; i < columns; i++) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(expiryMillis + (long) stepMillis * (i + 1));
            histogram.put(i, calendar);
        }

        return histogram;
    }

    private List<Calendar> extractDates(Article source) {

        final List<Calendar> dates = new LinkedList<Calendar>();
        for (Comment comment : source.getComments()) {
            Calendar date = Calendar.getInstance();
            date.setTime(comment.publishedDate());
            dates.add(date);
        }
        return dates;
    }

    @Override
    public String name() {
        return "CommentsFreshnessCriterion";
    }

}
