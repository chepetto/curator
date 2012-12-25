package org.curator.core.criterion;

import java.util.ArrayList;
import java.util.List;

import org.curator.core.analysis.Analyzable;
import org.curator.core.analysis.Analyzer;
import org.curator.core.constraint.Constraint;
import org.curator.core.constraint.Constraintable;

public class CriterionComposite<CRITERION extends Criterion> implements Analyzable, Constraintable {

    private final List<CRITERION> criteria = new ArrayList<CRITERION>();
    private final List<Double> weights = new ArrayList<Double>();
    private final List<Analyzer> analyzers = new ArrayList<Analyzer>();
    private final List<Constraint> constraints = new ArrayList<Constraint>();

    protected void addCriterion(CRITERION criterion) {
        addCriterion(criterion, 1d);
    }

    protected void addCriterion(CRITERION criterion, Double weight) {

        if (criterion == null) {
            throw new IllegalArgumentException("criterion is null");
        }

        if (weight < 0) {
            throw new IllegalArgumentException("Weight<0!");
        }
        criteria.add(criterion);
        weights.add(weight);
    }

    protected List<CRITERION> getCriteria() {
        return criteria;
    }

    protected List<Double> getWeights() {
        return weights;
    }


    @Override
    public void addAnalyzer(Analyzer analyzer) {
        analyzers.add(analyzer);
    }

    @Override
    public List<Analyzer> getAnalyzer() {
        return analyzers;
    }

    @Override
    public void addConstraint(Constraint constraint) {
        constraints.add(constraint);
    }

    @Override
    public List<Constraint> getConstraints() {
        return constraints;
    }
}
