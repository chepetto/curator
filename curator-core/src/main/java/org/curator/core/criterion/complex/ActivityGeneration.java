package org.curator.core.criterion.complex;

import org.curator.core.criterion.AbstractComplexCriterion;
import org.curator.core.criterion.simple.CommentCountCriterion;
import org.curator.core.criterion.simple.CommentReadingEaseCriterion;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.inject.Inject;

@Stateless
public class ActivityGeneration extends AbstractComplexCriterion {

    @Inject
    private CommentCountCriterion commentCountCriterion;
    @Inject
    private CommentReadingEaseCriterion readingEaseCriterion;

    public ActivityGeneration() {
        //
    }

    @PostConstruct
    public void onInit() {
        addCriterion(commentCountCriterion);
        addCriterion(readingEaseCriterion);
    }

    @Override
    public String name() {
        return "ActivityGeneration";
    }
}
