package org.curator.core.criterion;

import org.curator.core.model.MetricName;
import org.curator.core.util.VectorUtils;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class MultiplePerformance implements Performance {

    private Criterion criterion;
    private Map<MetricName, Double> details = new HashMap<MetricName, Double>(5);

    private List<Double> bestResult = new LinkedList<Double>();
    private List<Double> actualResult = new LinkedList<Double>();

    public MultiplePerformance(Criterion criterion) {
        this.criterion = criterion;
    }

    @Override
    public Criterion getCriterion() {
        return criterion;
    }

    public void addResult(MetricName metricName, double bestResult, double result) {
        addResult(bestResult, result);
        details.put(metricName, result);
    }

    public void addResult(double bestResult, double result) {
        this.bestResult.add(bestResult);
        this.actualResult.add(result);
    }

    @Override
    public Double getResult() {
        return VectorUtils.distance(
                bestResult.toArray(new Double[bestResult.size()]),
                actualResult.toArray(new Double[actualResult.size()]));
    }

    public Map<MetricName, Double> getDetails() {
        return details;
    }
}
