package org.curator.core.criterion.simple;

import org.curator.common.configuration.Configuration;
import org.curator.core.criterion.AbstractSimpleCriterion;
import org.apache.log4j.Logger;

import java.util.Calendar;
import java.util.Date;

public abstract class AbstractFreshnessCriterion extends AbstractSimpleCriterion {

    protected Double getFreshness(final Date date) {

        final Calendar expiry = Configuration.getFreshnessExpiry();

//    SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy");
//    System.out.println("expiry "+format.format(expiry.getTime()));
//    System.out.println("date "+format.format(date.getTime()));
//    System.out.println("now "+format.format(new Date()));

        final long now = new Date().getTime();
        final double diff = now - date.getTime();
//    System.out.println("diff "+format.format(new Date((long)diff)));
        final double maxDiff = now - expiry.getTimeInMillis();
//    System.out.println("maxDiff "+format.format(maxDiff));
        double result = 0d;
        if (diff < maxDiff) {
            result = 1 - diff / maxDiff;
        }
        return result;
    }

}
