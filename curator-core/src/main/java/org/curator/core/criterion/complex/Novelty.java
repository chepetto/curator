package org.curator.core.criterion.complex;

import org.curator.core.criterion.AbstractComplexCriterion;
import org.curator.core.criterion.simple.InverseOutgoingLinksCriterion;

import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.inject.Inject;

@Stateless
public class Novelty extends AbstractComplexCriterion {

    @Inject
    private  InverseOutgoingLinksCriterion linksCriterion;

    public Novelty() {
        //
    }

    @PostConstruct
    public void onInit() {
        addCriterion(linksCriterion);
    }

    @Override
    public String name() {
        return "Novelty";
    }

}
