package org.curator.core.criterion;

import java.util.LinkedList;
import java.util.List;

public class CompositePerformance implements Performance {

    private List<Performance> results = new LinkedList<Performance>();
    private Criterion criteron;
    private double total = 0d;

    public CompositePerformance(Criterion criterion) {
        this.criteron = criterion;
    }

    @Override
    public Double getResult() {
        if(results.isEmpty()) {
            return null;
        } else {
            return total/results.size();
        }
    }

    @Override
    public Criterion getCriterion() {
        return criteron;
    }

    public void addResult(Performance result) {
        results.add(result);
        total += result.getResult();
    }
}
