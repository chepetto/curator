package org.curator.core.criterion.simple;

import org.curator.common.configuration.Configuration;
import org.curator.common.exceptions.CuratorException;
import org.curator.common.model.Article;
import org.curator.common.model.Comment;
import org.curator.core.criterion.Goal;
import org.curator.core.criterion.Performance;
import org.curator.core.criterion.SinglePerformance;
import org.apache.log4j.Logger;

import javax.ejb.Stateless;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

@Stateless
@Deprecated
public class CommentsAverageFreshnessCriterion extends AbstractFreshnessCriterion {

    private static final Logger LOGGER = Logger.getLogger(CommentsAverageFreshnessCriterion.class);

    @Override
    public Performance evalCriterion(final Article source, final Goal goal) throws CuratorException {

        if (source == null) {
            throw new IllegalArgumentException("source is null");
        }

        if (source.getComments() == null) {
            return null;
        }

        List<Date> dates = extractDates(source);
        if (dates.isEmpty()) {
            return null;

        } else {

            Double freshness = getFreshness(findAverageOfDates(dates));
            LOGGER.trace("eval " + name() + ": " + freshness);
            return new SinglePerformance(this, freshness);
        }
    }

    protected Date findAverageOfDates(final List<Date> dates) {

        final long expiry = Configuration.getFreshnessExpiry().getTimeInMillis();

        long sum = 0l;
        for (Date date : dates) {
            sum += (date.getTime() - expiry);
        }

        return new Date(expiry + sum / dates.size());
    }

    private List<Date> extractDates(Article source) {

        final List<Date> dates = new LinkedList<Date>();
        for (Comment comment : source.getComments()) {
            dates.add(comment.publishedDate());
        }
        return dates;
    }

    @Override
    public String name() {
        return "CommentsAverageFreshnessCriterion";
    }

}
