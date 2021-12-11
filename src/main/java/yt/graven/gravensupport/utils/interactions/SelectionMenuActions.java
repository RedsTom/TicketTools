package yt.graven.gravensupport.utils.interactions;

import net.dv8tion.jda.api.events.interaction.SelectionMenuEvent;
import org.springframework.context.ApplicationContext;
import yt.graven.gravensupport.commands.ticket.create.interactions.FirstSentenceHandler;
import yt.graven.gravensupport.utils.exceptions.TicketException;

import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

public enum SelectionMenuActions {
    FIRST_SENTENCE("first-sentence", (context) -> context.getBean(FirstSentenceHandler.class)),
    ;

    private final String actionId;
    private final Function<ApplicationContext, IIInteractionAction<SelectionMenuEvent>> handler;

    SelectionMenuActions(String actionId, Function<ApplicationContext, IIInteractionAction<SelectionMenuEvent>> handler) {
        this.actionId = actionId;
        this.handler = handler;
    }

    public String getActionId() {
        return actionId;
    }

    public Function<ApplicationContext, IIInteractionAction<SelectionMenuEvent>> getHandler() {
        return handler;
    }

    public void run(ApplicationContext context, SelectionMenuEvent event) throws TicketException, IOException {
        handler.apply(context).run(event);
    }

    public static Optional<SelectionMenuActions> getFromActionId(String actionId) {
        return Arrays.stream(values())
            .filter(a -> Objects.equals(a.actionId, actionId))
            .findFirst();
    }
}
