package org.curator.core.criterion.simple;

import org.apache.log4j.Logger;
import org.curator.common.exceptions.CuratorException;
import org.curator.core.criterion.AbstractSimpleCriterion;
import org.curator.core.criterion.Goal;
import org.curator.core.criterion.Performance;
import org.curator.core.model.Article;
import org.curator.core.model.MetricName;
import org.curator.core.model.MetricProvider;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;

@LocalBean
@Stateless
public class ViewCountCriterion extends AbstractSimpleCriterion implements MetricProvider {

    private static final transient Logger LOGGER = Logger.getLogger(ViewCountCriterion.class);

    @Override
    public Performance evalCriterion(final Article source, final Goal goal) throws CuratorException {

        if (source == null) {
            throw new IllegalArgumentException("source is null");
        }

        // todo implement
//        Double views = source.getMetricResult(MetricName.VIEW_COUNT);

        return null;
    }


    @Override
    public void pushMetricResults(Article article) throws CuratorException {
        if (article.getViews() != null) {
            double views = getViewCount(article);
            article.addMetricResult(MetricName.VIEW_COUNT, views);
            LOGGER.trace("eval " + MetricName.VIEW_COUNT + ": " + views);
        }
    }

    private double getViewCount(Article article) {
        return article.getViews();
    }

    @Override
    public String name() {
        return "ViewCountCriterion";
    }

}
