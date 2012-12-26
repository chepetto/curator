package org.curator.common.model;

import org.curator.common.exceptions.CuratorException;

public interface MetricProvider {

    void pushMetricResults(Article article) throws CuratorException;
}
