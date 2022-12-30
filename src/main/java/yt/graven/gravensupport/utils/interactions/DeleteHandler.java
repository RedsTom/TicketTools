package yt.graven.gravensupport.utils.interactions;

import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;

public class DeleteHandler implements IIInteractionAction<ButtonInteractionEvent> {
  @Override
  public void run(ButtonInteractionEvent event) {
    try {
      event.deferEdit().queue();
      event.getMessage().delete().queue();
    } catch (Exception ignored) {
    }
  }
}
