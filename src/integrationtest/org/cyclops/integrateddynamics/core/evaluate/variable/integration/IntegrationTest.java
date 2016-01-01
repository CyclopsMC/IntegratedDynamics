package org.cyclops.integrateddynamics.core.evaluate.variable.integration;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for integration test methods..
 * @author rubensworks
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface IntegrationTest {
    Class<?> expected() default Void.class;
}
