package org.curator.core.constraint;

import org.curator.core.model.Article;

public interface Constraint {

    void validate(Article toEvaluate) throws ConstraintViolation;

}
