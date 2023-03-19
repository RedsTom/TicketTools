package yt.graven.gravensupport.database.repo;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import org.springframework.stereotype.Component;

@Component
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Repository {}
