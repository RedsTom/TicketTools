package org.redstom.botapi.injector;

import java.lang.annotation.*;

@Target({ElementType.METHOD, ElementType.CONSTRUCTOR,})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Inject {

    Class<?>[] value() default {};

}
