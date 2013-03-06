package org.curator.core.eval.impl;

import org.apache.log4j.Logger;
import org.curator.common.exceptions.CuratorException;
import org.curator.core.analysis.Analyzer;
import org.curator.core.constraint.Constraint;
import org.curator.core.constraint.ConstraintViolation;
import org.curator.core.constraint.impl.EnsureFreshness;
import org.curator.core.criterion.Criterion;
import org.curator.core.criterion.CriterionComposite;
import org.curator.core.criterion.Goal;
import org.curator.core.criterion.Performance;
import org.curator.core.criterion.complex.*;
import org.curator.core.eval.Evaluation;
import org.curator.core.eval.Evaluator;
import org.curator.core.model.Article;

import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.inject.Inject;

@Stateless
public class ArticleEvaluator extends CriterionComposite<Criterion> implements Evaluator<Evaluation> {

    private static transient final Logger LOGGER = Logger.getLogger(ArticleEvaluator.class);

    // -- CRITERIA -- --------------------------------------------------------------------------------------------------
//    @Inject
//    private Freshness freshness;
    @Inject
    private Novelty novelty;
    @Inject
    private Recognition recognition;
    @Inject
    private ActivityGeneration activityGeneration;
    @Inject
    private Eloquence eloquence;
    @Inject
    private ReaderDiscussion readerDiscussion;
    @Inject
    private Upcoming upcoming;

    // -- ANALYZER -- --------------------------------------------------------------------------------------------------

//    @Autowired
//    private KeywordAnalyzer keywordAnalyzer;
//    @Inject
//    private CategoryClassifier categoryClassifier;

    // -- CONSTRANTS -- ------------------------------------------------------------------------------------------------

    @Inject
    private EnsureFreshness ensureFreshness;


    public ArticleEvaluator() {
        // default
    }

    @PostConstruct
    public void onInit() {

//        addCriterion(freshness);
        addCriterion(novelty);
        addCriterion(recognition);
        addCriterion(activityGeneration);
        addCriterion(eloquence);
        addCriterion(readerDiscussion);
        addCriterion(upcoming);

//        addAnalyzer(keywordAnalyzer);
//        addAnalyzer(categoryClassifier);

//        addConstraint(ensureFreshness);

    }


    //@Override
    public Evaluation evaluate(final Article toEvaluate, final Goal goal) throws ConstraintViolation, CuratorException {

        if (toEvaluate == null) {
            throw new IllegalArgumentException("Article is null");
        }

        for (Constraint constraint : getConstraints()) {
            constraint.validate(toEvaluate);
        }

        ArticleEvaluation eval = new ArticleEvaluation(toEvaluate);
        for (Criterion criterion : getCriteria()) {
//            toEvaluate.getMetricResult(criterion)
            Performance performance = criterion.evalCriterion(toEvaluate, goal);
            LOGGER.trace(String.format("%s %4.4f", criterion, performance.getResult()));
            eval.addPerformance(criterion, performance);
        }

        for (Analyzer analyzer : getAnalyzer()) {
            analyzer.analyze(toEvaluate);
        }

        return eval;
    }

    @Override
    public void addCriterion(Criterion criterion) {
        super.addCriterion(criterion);
    }

//    public String getCriteriaAsString() {
//        StringBuilder b = new StringBuilder(getCriteria().size() * 10);
//        b.append('[');
//        Iterator<Criterion> i = getCriteria().iterator();
//        while (i.hasNext()) {
//            Criterion c = i.next();
//            b.append(c);
//            if (i.hasNext()) {
//                b.append(", ");
//            }
//        }
//        b.append(']');
//        return b.toString();
//    }

}
