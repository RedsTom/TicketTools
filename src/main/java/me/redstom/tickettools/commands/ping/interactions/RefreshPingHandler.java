package me.redstom.tickettools.commands.ping.interactions;

import lombok.RequiredArgsConstructor;
import me.redstom.tickettools.commands.ping.PingComputer;
import me.redstom.tickettools.utils.interactions.InteractionAction;
import me.redstom.tickettools.utils.messages.Embeds;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RefreshPingHandler implements InteractionAction<ButtonInteractionEvent> {

    private final PingComputer pingComputer;
    private final Embeds embeds;

    @Override
    public void run(ButtonInteractionEvent event) {
        InteractionHook edit = event.deferEdit().complete();

        pingComputer
                .update()
                .thenAccept(ignored -> embeds.ping(pingComputer).editReply(edit).queue());
    }
}
