package org.curator.core.criterion.simple;

import org.curator.common.configuration.Configuration;
import org.curator.common.exceptions.CuratorException;
import org.curator.common.model.Article;
import org.curator.common.model.MetricProvider;
import org.curator.core.criterion.Criterion;
import org.curator.core.criterion.Goal;
import org.curator.core.criterion.Performance;
import org.curator.core.criterion.SinglePerformance;
import org.apache.log4j.Logger;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;


@LocalBean
@Stateless
public class CommentCountCriterion implements Criterion, MetricProvider {

    private static final Logger LOGGER = Logger.getLogger(CommentCountCriterion.class);

    @Override
    public Performance evalCriterion(final Article source, final Goal goal) throws CuratorException {

        if (source == null) {
            throw new IllegalArgumentException("source is null");
        }
        if (source.getComments() == null) {
            return null;
        }

        int count = source.getComments().size();

        if (count==0) {
            LOGGER.trace("eval " + name() + ": 0");
            return new SinglePerformance(this, 0d);
        }

        if (count == 1) {
            LOGGER.trace("eval " + name() + ": 0.5");
            return new SinglePerformance(this, 0.5d);
        }

        int maxCommentCount = Configuration.getIntValue(Configuration.LIMIT_MAX_COMMENT_COUNT, 400);
        if (count >= maxCommentCount) {
            count = maxCommentCount;
        }

        final double result = Math.log(count) / Math.log(maxCommentCount);

        LOGGER.trace("eval " + name() + ": " + result);
        return new SinglePerformance(this, result);
    }

    @Override
    public void pushMetricResults(Article article) {
        // todo implement
    }

    @Override
    public String name() {
        return "CommentCountCriterion";
    }

}
