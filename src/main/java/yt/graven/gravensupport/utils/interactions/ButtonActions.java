package yt.graven.gravensupport.utils.interactions;

import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import org.springframework.context.ApplicationContext;
import yt.graven.gravensupport.commands.ping.interactions.RefreshPingHandler;
import yt.graven.gravensupport.commands.ticket.interactions.*;
import yt.graven.gravensupport.utils.exceptions.TicketException;

@RequiredArgsConstructor
public enum ButtonActions {
    DELETE("delete", (context) -> new DeleteHandler()),
    REFRESH_PING("refresh-ping", (context) -> context.getBean(RefreshPingHandler.class)),
    CONFIRM_MESSAGE("confirm-message", (context) -> context.getBean(ConfirmMessageHandler.class)),
    DENY_MESSAGE("deny-message", (context) -> context.getBean(DenyMessageHandler.class)),
    DELETE_MESSAGE("delete-message", (context) -> context.getBean(DeleteMessageHandler.class)),
    EDIT_MESSAGE("edit-message", (context) -> context.getBean(EditMessageHandler.class)),
    CONFIRM_OPENING("validate-opening", (context) -> context.getBean(ValidateOpeningHandler.class)),
    REPORT_USER("report-user", (context) -> context.getBean(ReportUserButtonHandler.class)),
    OPEN_WITH_REPORTED("open-with-reported", (context) -> context.getBean(OpenWithReportedHandler.class));

    private final String actionId;
    private final Function<ApplicationContext, InteractionAction<ButtonInteractionEvent>> handler;

    public void run(ApplicationContext context, ButtonInteractionEvent event) throws TicketException, IOException {
        handler.apply(context).run(event);
    }

    public static Optional<ButtonActions> getFromActionId(String actionId) {
        return Arrays.stream(values())
                .filter(a -> Objects.equals(a.actionId, actionId))
                .findFirst();
    }
}
