package yt.graven.gravensupport.utils.commands;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import org.springframework.stereotype.Component;

@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface Command {}
