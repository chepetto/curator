package org.curator.core.constraint;

import org.curator.common.model.Article;

public interface Constraint {

    void validate(Article toEvaluate) throws ConstraintViolation;

}
