package org.redstom.botapi.events;

import org.javacord.api.event.Event;

import java.lang.annotation.*;

/**
 * Declares a method as an event receiver
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface EventReceiver {

    /**
     * The type of event to register
     */
    Class<? extends Event> value();

}
