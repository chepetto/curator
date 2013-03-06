package org.curator.core.constraint.impl;

import org.curator.common.configuration.Configuration;
import org.curator.core.constraint.Constraint;
import org.curator.core.constraint.ConstraintViolation;
import org.curator.core.model.Article;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import java.util.Date;

@LocalBean
@Stateless
public class EnsureFreshness implements Constraint {

    private final transient Date bestBefore;

    public EnsureFreshness(final Date bestBefore) {
        this.bestBefore = bestBefore;
    }

    public EnsureFreshness() {
        this.bestBefore = Configuration.getBestBeforeDate();
    }

    @Override
    public void validate(final Article toEvaluate) throws ConstraintViolation {
        if (bestBefore.after(toEvaluate.getDate())) {
            throw new ConstraintViolation(String.format("Article %s violates freshness", toEvaluate.getUrl()));
        }
    }

}
