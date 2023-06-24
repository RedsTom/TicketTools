package me.redstom.tickettools.utils.interactions;

import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import me.redstom.tickettools.commands.ticket.interactions.FirstSentenceHandler;
import me.redstom.tickettools.commands.ticket.interactions.OpeningReasonHandler;
import me.redstom.tickettools.utils.exceptions.TicketException;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import org.springframework.context.ApplicationContext;

@RequiredArgsConstructor
public enum SelectionMenuActions {
    FIRST_SENTENCE("first-sentence", (context) -> context.getBean(FirstSentenceHandler.class)),
    OPENING_REASON("opening-menu", (context) -> context.getBean(OpeningReasonHandler.class));

    private final String actionId;
    private final Function<ApplicationContext, InteractionAction<StringSelectInteractionEvent>> handler;

    public void run(ApplicationContext context, StringSelectInteractionEvent event)
            throws TicketException, IOException {
        handler.apply(context).run(event);
    }

    public static Optional<SelectionMenuActions> getFromActionId(String actionId) {
        return Arrays.stream(values())
                .filter(a -> Objects.equals(a.actionId, actionId))
                .findFirst();
    }
}
