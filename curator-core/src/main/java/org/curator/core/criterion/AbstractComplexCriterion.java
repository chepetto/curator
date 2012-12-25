package org.curator.core.criterion;

import java.util.List;

import org.curator.common.exceptions.CuratorException;
import org.curator.common.model.Article;
import org.apache.log4j.Logger;

public abstract class AbstractComplexCriterion extends CriterionComposite<Criterion> implements Criterion {

    private static final transient Logger LOGGER = Logger.getLogger(AbstractComplexCriterion.class);

    public abstract String name();

    @Override
    public Performance evalCriterion(final Article source, final Goal goal) throws CuratorException {
        CompositePerformance performance = new CompositePerformance(this);

        for (Criterion criterion : getCriteria()) {
            Performance result = criterion.evalCriterion(source, goal);
            if(result==null) {
                LOGGER.trace("No performance for "+criterion.name());
            } else {
                performance.addResult(result);
            }
        }

        return performance;
    }

    public String toString() {
        StringBuilder buffer = new StringBuilder(150);
        List<Criterion> criteria = getCriteria();
        for(int i=0; i<criteria.size(); i++) {
            buffer.append(criteria.get(i).toString());
            Double weight = getWeights().get(i);
            if(weight!=1) {
                buffer.append("^").append(weight);
            }

            if (i+1<criteria.size()) {
                buffer.append(", ");
            }
        }

        return name() + " {" + buffer.toString() + "}";
    }
}
