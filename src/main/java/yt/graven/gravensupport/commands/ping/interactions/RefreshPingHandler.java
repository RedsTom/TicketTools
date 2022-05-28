package yt.graven.gravensupport.commands.ping.interactions;

import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import yt.graven.gravensupport.commands.ping.PingManager;
import yt.graven.gravensupport.utils.interactions.IIInteractionAction;

@Component
public class RefreshPingHandler implements IIInteractionAction<ButtonInteractionEvent> {

    @Autowired
    private PingManager pingManager;

    @Override
    public void run(ButtonInteractionEvent event) {
        event.deferEdit().setEmbeds(pingManager.compute()).queue();
    }
}
