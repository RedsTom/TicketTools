package org.redstom.botapi.events;

import java.lang.annotation.*;

/**
 * Declares a class as a class with {@link EventReceiver} inside
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface EventBus {


}
