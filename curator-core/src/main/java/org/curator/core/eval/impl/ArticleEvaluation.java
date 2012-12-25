package org.curator.core.eval.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import org.curator.common.model.Article;
import org.curator.core.criterion.Criterion;
import org.curator.core.criterion.Performance;
import org.curator.core.eval.Evaluation;
import org.curator.core.util.VectorUtils;
import org.apache.log4j.Logger;

public class ArticleEvaluation implements Evaluation {

    private static final Logger LOGGER = Logger.getLogger(ArticleEvaluation.class);

    private final transient Map<Criterion, Performance> evaluations = new HashMap<Criterion, Performance>(15);
    private final transient Article article;

    public ArticleEvaluation(Article article) {
        this.article = article;
    }

    @Override
    public void addPerformance(final Criterion criterion, final Performance performance) {
        evaluations.put(criterion, performance);
    }

    @Override
    public Double[] toVector() {
        Vector<Double> vector = new Vector<Double>();
        for (Performance performance : evaluations.values()) {
            Double result = performance.getResult();
            if(result==null) {
                LOGGER.trace("No performance for "+performance.getCriterion().name());
            } else {
                vector.add(result);
            }
        }
        return vector.toArray(new Double[vector.size()]);
    }

    @Override
    public Double quality() {
        Double[] vector = toVector();
        if(vector.length==0) {
            return null;
        } else {
            return VectorUtils.distance(getMaxQuality(vector.length), vector);
        }
    }

    private Double[] getMaxQuality(int length) {
        Double[] v = new Double[length];
        for (int i = 0; i < v.length; i++) {
            v[i] = 1d;
        }
        return v;
    }

    @Override
    public String toString() {
        StringBuilder buffer = new StringBuilder(200);
        buffer.append("{\n");
        buffer.append(String.format("\turi:\t%s\n", article.getUrl()));

        for (Criterion criterion : evaluations.keySet()) {
            buffer.append('\t');
            buffer.append(criterion.name());
            buffer.append(":\t");
            buffer.append(evaluations.get(criterion));
            buffer.append('\n');
        }
        buffer.append('}');
        return buffer.toString();
    }

    @Override
    public int compareTo(final Evaluation eval) {
        int c = eval.quality().compareTo(quality());
        return c == 0 ? 1 : c;
    }

    @Override
    public boolean equals(final Object obj) {
        return obj instanceof ArticleEvaluation && ((ArticleEvaluation) obj).article().equals(this);
    }

    @Override
    public Article article() {
        return article;
    }
}
