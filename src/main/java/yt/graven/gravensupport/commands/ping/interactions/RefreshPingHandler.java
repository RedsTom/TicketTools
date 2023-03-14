package yt.graven.gravensupport.commands.ping.interactions;

import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import org.springframework.stereotype.Component;
import yt.graven.gravensupport.commands.ping.PingComputer;
import yt.graven.gravensupport.utils.interactions.IIInteractionAction;
import yt.graven.gravensupport.utils.messages.Embeds;

@Component
@RequiredArgsConstructor
public class RefreshPingHandler implements IIInteractionAction<ButtonInteractionEvent> {

    private final PingComputer pingComputer;
    private final Embeds embeds;

    @Override
    public void run(ButtonInteractionEvent event) {
        InteractionHook edit = event.deferEdit().complete();

        pingComputer.update()
                .thenAccept(ignored -> embeds.ping(pingComputer)
                        .editReply(edit)
                        .queue());
    }
}
