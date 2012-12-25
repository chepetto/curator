package org.curator.core.criterion;

import org.curator.common.exceptions.CuratorException;
import org.curator.common.model.Article;


public interface Criterion {

    Performance evalCriterion(Article source, Goal goal) throws CuratorException;

    String name();

}
