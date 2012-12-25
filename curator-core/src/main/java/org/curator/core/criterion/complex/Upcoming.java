package org.curator.core.criterion.complex;

import org.curator.core.criterion.AbstractComplexCriterion;

import javax.ejb.Stateless;

@Stateless
public class Upcoming extends AbstractComplexCriterion {

    public Upcoming() {
        // todo survey attraction generation over time
    }

    @Override
    public String name() {
        return "Upcoming";
    }

}
