package org.curator.core.criterion.simple;

import org.curator.common.exceptions.CuratorException;
import org.curator.common.model.Article;
import org.curator.common.model.Comment;
import org.curator.common.model.MetricName;
import org.curator.common.model.MetricProvider;
import org.curator.core.criterion.*;
import org.apache.log4j.Logger;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import java.util.List;
import java.util.Locale;


@LocalBean
@Stateless
public class CommentReadingEaseCriterion extends AbstractReadingEaseCriterion implements MetricProvider {

    private static final transient Logger LOGGER = Logger.getLogger(CommentReadingEaseCriterion.class);

    @Override
    public Performance evalCriterion(final Article source, final Goal goal) throws CuratorException {

        if (source == null) {
            throw new IllegalArgumentException("source is null");
        }
        final List<Comment> comments = source.getComments();

        if (comments == null) {
            return null;
        }
        if (comments.isEmpty()) {
            return new SinglePerformance(this, 0d);
        }

        MultiplePerformance result = new MultiplePerformance(this);

        final double amdahl = getAmdahlIndex(source.getLocale(), source.getComments());
        LOGGER.trace("eval " + MetricName.AMDAHL + ": " + amdahl);
        result.addResult(MetricName.AMDAHL, 1d, amdahl);

        final double flesch = getFleschIndex(source.getLocale(), source.getComments());
        LOGGER.trace("eval " + MetricName.FLESCH + ": " + flesch);
        result.addResult(MetricName.FLESCH, 1d, flesch);

        final double smog = getSMOGIndex(source.getLocale(), source.getComments());
        LOGGER.trace("eval " + MetricName.SMOG + ": " + smog);
        result.addResult(MetricName.SMOG, 1d, smog);

        return result;

    }

    @Override
    public void pushMetricResults(Article article) {
        // todo implement
    }

    private double getSMOGIndex(Locale locale, List<Comment> comments) {
        double total = 0d;
        for(Comment c:comments) {
            total += getSMOGIndex(locale, c);
        }
        return total/comments.size();
    }

    private double getFleschIndex(Locale locale, List<Comment> comments) {
        double total = 0d;
        for(Comment c:comments) {
            total += getFleschIndex(locale, c);
        }
        return total/comments.size();
    }

    private double getAmdahlIndex(Locale locale, List<Comment> comments) {

        double total = 0d;
        for(Comment c:comments) {
            total += getAmdahlIndex(locale, c);
        }
        return total/comments.size();
    }

    @Override
    public String name() {
        return "CommentReadingEaseCriterion";
    }
}
