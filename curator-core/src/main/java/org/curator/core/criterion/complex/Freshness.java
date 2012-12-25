package org.curator.core.criterion.complex;

import org.curator.core.criterion.AbstractComplexCriterion;
import org.curator.core.criterion.simple.ArticleFreshnessCriterion;
import org.curator.core.criterion.simple.CommentsAverageFreshnessCriterion;
import org.curator.core.criterion.simple.CommentsFreshnessCriterion;

import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.inject.Inject;

@Stateless
public class Freshness extends AbstractComplexCriterion {

    @Inject
    private ArticleFreshnessCriterion articleFreshnessCriterion;
    @Inject
    private CommentsFreshnessCriterion commentsfreshnessCriterion;

    public Freshness() {
        //
    }

    @PostConstruct
    public void onInit() {

        addCriterion(articleFreshnessCriterion);
        addCriterion(commentsfreshnessCriterion);

    }

    public String name() {
        return "Freshness";
    }

}
