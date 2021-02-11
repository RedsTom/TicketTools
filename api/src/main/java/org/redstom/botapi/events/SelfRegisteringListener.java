package org.redstom.botapi.events;

import java.lang.annotation.*;

/**
 * The type that annotates this @interface must also annotate the {@link EventBus}
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SelfRegisteringListener {

}
