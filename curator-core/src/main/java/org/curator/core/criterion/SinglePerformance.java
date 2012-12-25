package org.curator.core.criterion;

public class SinglePerformance implements Performance {

    private double result;
    private Criterion criterion;

    public SinglePerformance(Criterion criterion, double result) {
        this.result = result;
        this.criterion = criterion;
    }

    @Override
    public Double getResult() {
        return result;
    }

    @Override
    public Criterion getCriterion() {
        return criterion;
    }
}
