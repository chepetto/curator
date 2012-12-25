package org.curator.common.configuration;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * @author Daniel Scheidle, daniel.scheidle@ucs.at
 */
@Target(FIELD)
@Retention(RUNTIME)
public @interface ConfigurationProperty {

    public String value();

    public boolean mandatory() default false;

    public String defaultValue() default "";
}
