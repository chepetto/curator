package org.curator.core.eval;

import org.curator.common.model.Article;
import org.curator.core.criterion.Criterion;
import org.curator.core.criterion.Performance;

public interface Evaluation extends Comparable<Evaluation> {

    void addPerformance(Criterion criterion, Performance performance);

    Double[] toVector();

    Double quality();

    Article article();

}
