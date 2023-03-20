package yt.graven.gravensupport.configuration.annotations;

import org.springframework.beans.factory.annotation.Qualifier;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@Qualifier("reportsChannel")
public @interface ReportsChannel {
}
