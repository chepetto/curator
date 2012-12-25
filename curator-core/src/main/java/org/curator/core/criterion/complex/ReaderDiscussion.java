package org.curator.core.criterion.complex;

import org.curator.core.criterion.AbstractComplexCriterion;
import org.curator.core.criterion.simple.CommentCountCriterion;
import org.curator.core.criterion.simple.CommentReadingEaseCriterion;
import org.curator.core.criterion.simple.RecurringUserCriterion;

import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.inject.Inject;

@Stateless
public class ReaderDiscussion extends AbstractComplexCriterion {

    @Inject
    private CommentCountCriterion commentCountCriterion;
    @Inject
    private RecurringUserCriterion recurringUserCriterion;
    @Inject
    private CommentReadingEaseCriterion commentReadingEaseCriterion;

    public ReaderDiscussion() {
        //
    }

    @PostConstruct
    public void onInit() {
        addCriterion(commentCountCriterion);
        addCriterion(recurringUserCriterion);
        addCriterion(commentReadingEaseCriterion);
    }

    @Override
    public String name() {
        return "ReaderDiscussion";
    }

}
