package org.curator.core.status;

import org.codehaus.jackson.map.annotate.JsonSerialize;

import java.io.Serializable;

@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class FeedsStatus implements Serializable {

    private long totalFeedCount;

    public long getTotalFeedCount() {
        return totalFeedCount;
    }

    public void setTotalFeedCount(long totalFeedCount) {
        this.totalFeedCount = totalFeedCount;
    }
}
