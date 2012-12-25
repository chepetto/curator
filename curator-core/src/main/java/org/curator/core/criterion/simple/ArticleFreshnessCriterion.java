package org.curator.core.criterion.simple;

import org.curator.common.exceptions.CuratorException;
import org.curator.common.model.Article;
import org.curator.common.model.MetricName;
import org.curator.common.model.MetricProvider;
import org.curator.core.criterion.Goal;
import org.curator.core.criterion.Performance;
import org.curator.core.criterion.SinglePerformance;
import org.apache.log4j.Logger;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;

@LocalBean
@Stateless
public class ArticleFreshnessCriterion extends AbstractFreshnessCriterion implements MetricProvider {

    private static final Logger LOGGER = Logger.getLogger(ArticleFreshnessCriterion.class);

    @Override
    public Performance evalCriterion(final Article source, final Goal goal) throws CuratorException {

        // -- ASSERTIONS -- --------------------------------------------------------------------------------------------
        if (source == null) {
            throw new IllegalArgumentException("source is null");
        }
        if(source.getDate()==null) {
            return null;
        }
        if(!source.hasMetricResult(MetricName.ARTICLE_FRESHNESS)) {
            LOGGER.warn("Too few metric results: "+MetricName.ARTICLE_FRESHNESS);
            return null;
        }
        // -- ----------------------------------------------------------------------------------------------------------

        double freshness = source.getMetricResult(MetricName.ARTICLE_FRESHNESS);
        return new SinglePerformance(this, freshness);
    }

    @Override
    public String name() {
        return "ArticleFreshnessCriterion";
    }

    @Override
    public void pushMetricResults(Article article) throws CuratorException {
        double freshness = getFreshness(article.getDate());
        article.addMetricResult(MetricName.ARTICLE_FRESHNESS, freshness);
        LOGGER.trace("eval " + name() + ": " + freshness);
    }
}
