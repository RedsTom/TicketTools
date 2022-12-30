package yt.graven.gravensupport.commands.ping.interactions;

import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import org.springframework.stereotype.Component;
import yt.graven.gravensupport.commands.ping.PingManager;
import yt.graven.gravensupport.utils.interactions.IIInteractionAction;

@Component
@RequiredArgsConstructor
public class RefreshPingHandler implements IIInteractionAction<ButtonInteractionEvent> {

  private final PingManager pingManager;

  @Override
  public void run(ButtonInteractionEvent event) {
    event.deferEdit().setEmbeds(pingManager.compute()).queue();
  }
}
