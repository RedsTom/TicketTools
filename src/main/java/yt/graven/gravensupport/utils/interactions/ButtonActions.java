package yt.graven.gravensupport.utils.interactions;

import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import org.springframework.context.ApplicationContext;
import yt.graven.gravensupport.commands.help.interactions.NextPageHandler;
import yt.graven.gravensupport.commands.help.interactions.PrevPageHandler;
import yt.graven.gravensupport.commands.ping.interactions.RefreshPingHandler;
import yt.graven.gravensupport.commands.ticket.create.interactions.ConfirmMessageHandler;
import yt.graven.gravensupport.commands.ticket.create.interactions.DeleteMessageHandler;
import yt.graven.gravensupport.commands.ticket.create.interactions.DenyMessageHandler;
import yt.graven.gravensupport.commands.ticket.create.interactions.ValidateOpeningHandler;
import yt.graven.gravensupport.utils.exceptions.TicketException;

import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

public enum ButtonActions {
    DELETE("delete", (context) -> new DeleteHandler()),
    PREV_PAGE("prev-page", (context) -> context.getBean(PrevPageHandler.class)),
    NEXT_PAGE("next-page", (context) -> context.getBean(NextPageHandler.class)),
    REFRESH_PING("refresh-ping", (context) -> context.getBean(RefreshPingHandler.class)),
    CONFIRM_MESSAGE("confirm-message", (context) -> context.getBean(ConfirmMessageHandler.class)),
    DENY_MESSAGE("deny-message", (context) -> context.getBean(DenyMessageHandler.class)),
    DELETE_MESSAGE("delete-message", (context) -> context.getBean(DeleteMessageHandler.class)),
    CONFIRM_OPENING("validate-opening", (context) -> context.getBean(ValidateOpeningHandler.class)),
    ;

    private final String actionId;
    private final Function<ApplicationContext, IIInteractionAction<ButtonClickEvent>> handler;

    ButtonActions(String actionId, Function<ApplicationContext, IIInteractionAction<ButtonClickEvent>> handler) {
        this.actionId = actionId;
        this.handler = handler;
    }

    public String getActionId() {
        return actionId;
    }

    public Function<ApplicationContext, IIInteractionAction<ButtonClickEvent>> getHandler() {
        return handler;
    }

    public void run(ApplicationContext context, ButtonClickEvent event) throws TicketException, IOException {
        handler.apply(context).run(event);
    }

    public static Optional<ButtonActions> getFromActionId(String actionId) {
        return Arrays.stream(values())
            .filter(a -> Objects.equals(a.actionId, actionId))
            .findFirst();
    }
}
