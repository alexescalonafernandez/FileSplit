package io.github.alexescalonafernandez.filesplit.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by alexander.escalona on 11/09/2018.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Required {
    /* Represents the Split operation where is required.
     * Defaults any means in any operations is needed.
     */
    OperationMode value() default OperationMode.ANY;

    /**
     *
     * @return  the priority of the required data
     */
    int priority();

    String argumentName();
}
