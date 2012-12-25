package org.curator.core.constraint;

@SuppressWarnings("serial")
public class ConstraintViolation extends Exception {

    public ConstraintViolation(final String message) {
        super(message);
    }

}
