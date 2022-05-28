package yt.graven.gravensupport.utils.interactions;

import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import org.springframework.context.ApplicationContext;
import yt.graven.gravensupport.commands.ticket.create.interactions.OtherOpeningReasonHandler;
import yt.graven.gravensupport.utils.exceptions.TicketException;

import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

public enum ModalActions {
    OPENING_REASON("op-other-reason", (context) -> context.getBean(OtherOpeningReasonHandler.class))
    ;

    private final String actionId;
    private final Function<ApplicationContext, IIInteractionAction<ModalInteractionEvent>> handler;

    ModalActions(String actionId, Function<ApplicationContext, IIInteractionAction<ModalInteractionEvent>> handler) {
        this.actionId = actionId;
        this.handler = handler;
    }

    public String getActionId() {
        return actionId;
    }

    public Function<ApplicationContext, IIInteractionAction<ModalInteractionEvent>> getHandler() {
        return handler;
    }

    public void run(ApplicationContext context, ModalInteractionEvent event) throws TicketException, IOException {
        handler.apply(context).run(event);
    }

    public static Optional<ModalActions> getFromActionId(String actionId) {
        return Arrays.stream(values())
            .filter(a -> Objects.equals(a.actionId, actionId))
            .findFirst();
    }
}