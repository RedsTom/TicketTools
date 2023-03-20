package yt.graven.gravensupport.configuration.annotations;

import org.springframework.beans.factory.annotation.Qualifier;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Specifies that the annotated element should be injected with the ticket category.
 * Can also be used to specify that the provided parameter should be the ticket category.
 */
@Retention(RetentionPolicy.RUNTIME)
@Qualifier("ticketCategory")
public @interface TicketCategory {
}
