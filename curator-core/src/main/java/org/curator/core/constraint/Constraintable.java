package org.curator.core.constraint;

import java.util.List;

public interface Constraintable {

    void addConstraint(Constraint constraint);

    List<Constraint> getConstraints();
}
