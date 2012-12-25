package org.curator.core.criterion.complex;

import org.curator.core.criterion.AbstractComplexCriterion;
import org.curator.core.criterion.simple.IncomingLinksCriterion;
import org.curator.core.criterion.simple.ViewCountCriterion;

import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.inject.Inject;

@Stateless
public class Recognition extends AbstractComplexCriterion {

    @Inject
    private IncomingLinksCriterion incomingLinksCriterion;
    @Inject
    private ViewCountCriterion viewCountCriterion;

    public Recognition() {
        //
    }

    @PostConstruct
    public void onInit() {
        addCriterion(incomingLinksCriterion);
        addCriterion(viewCountCriterion);
    }

    @Override
    public String name() {
        return "Recognition";
    }

}
