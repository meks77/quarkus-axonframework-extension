package at.meks.quarkiverse.axon.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Define the ID class for the EventSourcedEntity.
 * This annotation is only necessary, if the ID is not of type String.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface IdType {

    /**
     * The class of the ID
     */
    Class<?> value();

}
