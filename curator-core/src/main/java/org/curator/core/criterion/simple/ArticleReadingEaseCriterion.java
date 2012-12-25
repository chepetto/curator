package org.curator.core.criterion.simple;

import org.apache.commons.lang.StringUtils;
import org.curator.common.exceptions.CuratorException;
import org.curator.common.model.Article;
import org.curator.common.model.MetricName;
import org.curator.common.model.MetricProvider;
import org.curator.core.Constants;
import org.curator.core.criterion.*;
import org.apache.log4j.Logger;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import java.util.Locale;


@LocalBean
@Stateless
public class ArticleReadingEaseCriterion extends AbstractReadingEaseCriterion implements MetricProvider {

    private static final transient Logger LOGGER = Logger.getLogger(ArticleReadingEaseCriterion.class);

    public ArticleReadingEaseCriterion() {
        //
    }


    @Override
    public Performance evalCriterion(final Article source, final Goal goal) throws CuratorException {

        // -- ASSERTIONS -- --------------------------------------------------------------------------------------------
        if (source == null) {
            throw new IllegalArgumentException("source is null");
        }
        if (source.getContent() == null || source.getContent().getText() == null) {
            return null;
        }
        if (!source.hasMetricResult(MetricName.SMOG)) {
            LOGGER.warn(String.format("Too few metric results: %s",
                    MetricName.SMOG
            ));
            return null;
        }
        // -- ----------------------------------------------------------------------------------------------------------

        if (StringUtils.isBlank(source.getContent().getText())) {
            return new SinglePerformance(this, 0d);
        }

        MultiplePerformance result = new MultiplePerformance(this);

        // deutsch
        if (Locale.GERMAN.equals(source.getLocale())) {
            final double amdahl = source.getMetricResult(MetricName.AMDAHL);
            addMetricResult(result, MetricName.AMDAHL, amdahl, Constants.AMDAHL_MIN_READABILITY, Constants.AMDAHL_MAX_READABILITY);
        }
        // flesch ist für englisch
        if (Locale.ENGLISH.equals(source.getLocale())) {
            final double flesch = source.getMetricResult(MetricName.FLESCH);
            addMetricResult(result, MetricName.FLESCH, flesch, Constants.FLESCH_MIN_READYBILITY, Constants.FLESCH_MAX_READABILITY);
        }

        final double smog = source.getMetricResult(MetricName.SMOG);
        addMetricResult(result, MetricName.SMOG, smog, Constants.SMOG_MIN_READABILITY, Constants.SMOG_MAX_READABILITY);

        return result;

    }

    @Override
    public String name() {
        return "ArticleReadingEaseCriterion";
    }

    @Override
    public void pushMetricResults(Article article) throws CuratorException {

        if (article.getContent() == null) {
            LOGGER.trace("No content available " + article.getUrl());
            return;
        }

        // deutsch
        if (Locale.GERMAN.equals(article.getLocale())) {
            final double amdahl = getAmdahlIndex(article.getLocale(), article.getContent());
            article.addMetricResult(MetricName.AMDAHL, amdahl);
            LOGGER.trace("eval " + MetricName.AMDAHL + ": " + amdahl);
        }
        // flesch ist für englisch
        if (Locale.ENGLISH.equals(article.getLocale())) {
            final double flesch = getFleschIndex(article.getLocale(), article.getContent());
            article.addMetricResult(MetricName.FLESCH, flesch);
            LOGGER.trace("eval " + MetricName.FLESCH + ": " + flesch);
        }

        final double smog = getSMOGIndex(article.getLocale(), article.getContent());
        article.addMetricResult(MetricName.SMOG, smog);
        LOGGER.trace("eval " + MetricName.SMOG + ": " + smog);
    }
}
