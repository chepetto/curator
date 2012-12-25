package org.curator.core.eval;

import org.curator.common.exceptions.CuratorException;
import org.curator.core.constraint.ConstraintViolation;
import org.curator.core.constraint.Constraintable;
import org.curator.common.model.Article;
import org.curator.core.criterion.Criterion;
import org.curator.core.criterion.Goal;

public interface Evaluator<EVALUATION extends Evaluation> extends Constraintable {

    EVALUATION evaluate(Article toEvaluate, Goal goal) throws ConstraintViolation, CuratorException;

    void addCriterion(Criterion criterion);

}
