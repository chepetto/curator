package org.curator.core.model;

import java.io.Serializable;

public class MetricResultId implements Serializable {

    private MetricName metricName;
    private long articelId;

    public MetricName getMetricName() {
        return metricName;
    }

    public void setMetricName(MetricName metricName) {
        this.metricName = metricName;
    }

    public long getArticelId() {
        return articelId;
    }

    public void setArticelId(long articelId) {
        this.articelId = articelId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MetricResultId that = (MetricResultId) o;

        if (articelId != that.articelId) return false;
        if (metricName != that.metricName) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = metricName != null ? metricName.hashCode() : 0;
        result = 31 * result + (int) (articelId ^ (articelId >>> 32));
        return result;
    }
}
