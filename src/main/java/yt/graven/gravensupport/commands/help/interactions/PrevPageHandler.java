package yt.graven.gravensupport.commands.help.interactions;

import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import org.springframework.stereotype.Component;
import yt.graven.gravensupport.commands.help.HelpManager;
import yt.graven.gravensupport.utils.interactions.IInteractionAction;

@Component
@RequiredArgsConstructor
public class PrevPageHandler implements IInteractionAction<ButtonInteractionEvent> {

  private final HelpManager helpManager;

  @Override
  public void run(ButtonInteractionEvent event) {
    event.deferEdit().setEmbeds(helpManager.getPrevPage(event.getMessageIdLong())).queue();
  }
}
