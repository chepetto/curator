package org.curator.core.criterion.simple;

import org.apache.log4j.Logger;
import org.curator.common.configuration.Configuration;
import org.curator.common.exceptions.CuratorException;
import org.curator.core.criterion.AbstractSimpleCriterion;
import org.curator.core.criterion.Goal;
import org.curator.core.criterion.Performance;
import org.curator.core.criterion.SinglePerformance;
import org.curator.core.model.Article;
import org.curator.core.model.MetricName;
import org.curator.core.model.MetricProvider;
import org.json.JSONObject;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

@LocalBean
@Stateless
public class IncomingLinksCriterion extends AbstractSimpleCriterion implements MetricProvider {

    private static final transient Logger LOGGER = Logger.getLogger(IncomingLinksCriterion.class);

    private final transient Map<String, Integer> tweetCount = new HashMap<String, Integer>(300);

    @Override
    public Performance evalCriterion(final Article source, final Goal goal) throws CuratorException {

        // -- ASSERTIONS -- --------------------------------------------------------------------------------------------
        if (source == null) {
            throw new IllegalArgumentException("source is null");
        }
        if (source.getUrl() == null) {
            return null;
        }
//        if(!source.hasMetricResult(MetricName.TWEET_COUNT)) {
//            LOGGER.warn("Too few metric results: "+MetricName.TWEET_COUNT);
//            return null;
//        }
        // -- ----------------------------------------------------------------------------------------------------------

        final Double tweetCount = source.getMetricResult(MetricName.TWEET_COUNT);

        double result;
        if (tweetCount == null) {
            result = 0d;

        } else if (tweetCount > 60) {
            result = 1d;

        } else {
            result = tweetCount / 60d;
        }

        LOGGER.trace("eval " + name() + ": " + result);
        return new SinglePerformance(this, result);
    }

    @Override
    public void pushMetricResults(Article article) throws CuratorException {
        double tweetCount = getTweetCount(article);
        article.addMetricResult(MetricName.TWEET_COUNT, tweetCount);
        LOGGER.trace("eval " + MetricName.TWEET_COUNT + ": " + tweetCount);
    }

    private int getTweetCount(final Article source) {
        final String url = source.getUrl();

        if (tweetCount.containsKey(url)) {
            return tweetCount.get(url);
        }

        Scanner scanner = null;
        try {
            Thread.sleep(Configuration.getIntValue(Configuration.SERVICE_TWITTER_REQ_DELAY_MSEC, 2000));
            scanner = new Scanner(new URL("http://urls.api.twitter.com/1/urls/count.json?url=" + url).openStream());
            final StringBuilder buffer = new StringBuilder(400);
            while (scanner.hasNext()) {
                buffer.append(scanner.next());
            }
            final JSONObject jdata = new JSONObject(buffer.toString());
            final int count = Integer.parseInt(String.valueOf(jdata.get("count")));
            tweetCount.put(url, count);

            return count;
        } catch (Exception e) {
            LOGGER.error(String.format("evalCriterion uri %s. message: %s", source.getUrl(), e.getMessage()));
            LOGGER.debug(e);
        } finally {
            if (scanner != null) {
                scanner.close();
            }
        }
        return 0;
    }

    @Override
    public String name() {
        return "IncomingLinksCriterion";
    }

}
