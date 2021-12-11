package yt.graven.gravensupport.commands.ping.interactions;

import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import org.springframework.beans.factory.annotation.Autowired;
import yt.graven.gravensupport.commands.ping.PingManager;
import yt.graven.gravensupport.utils.interactions.IIInteractionAction;

public class RefreshPingHandler implements IIInteractionAction<ButtonClickEvent> {

    @Autowired
    private PingManager pingManager;

    @Override
    public void run(ButtonClickEvent event) {
        event.deferEdit().setEmbeds(pingManager.compute()).queue();
    }
}
