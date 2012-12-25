package org.curator.core.criterion;

import org.curator.core.util.VectorUtils;

public abstract class AbstractSimpleCriterion implements Criterion {

    private Goal goal;

    public Goal getGoal() {
        return goal;
    }

    public void setGoal(Goal goal) {
        this.goal = goal;
    }

    public String toString() {
        return name();
    }

    protected Double getSimilarity(final Double[] a, final Double b[]) {
        return VectorUtils.distance(a, b);
    }
}
