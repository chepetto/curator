package org.curator.core.criterion.complex;

import org.curator.core.criterion.AbstractComplexCriterion;
import org.curator.core.criterion.simple.ArticleReadingEaseCriterion;
import org.curator.core.criterion.simple.ThesaurusCriterion;

import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.inject.Inject;


@Stateless
public class Eloquence extends AbstractComplexCriterion {


    @Inject
    private ThesaurusCriterion thesaurusCriterion;
    @Inject
    private ArticleReadingEaseCriterion readingEaseCriterion;

    public Eloquence() {
        //
    }

//    public Eloquence(Goal goal) {
//        // wortschatz
//        addCriterion(new ThesaurusCriterion(goal), 1d);
//        // einfachheit
//        addCriterion(new ArticleReadingEaseCriterion(goal), 2d);
//    }

    @PostConstruct
    public void onInit() {
        // wortschatz
        addCriterion(thesaurusCriterion, 1d);
        // einfachheit
        addCriterion(readingEaseCriterion, 2d);

    }


    @Override
    public String name() {
        return "Eloquence";
    }
}
