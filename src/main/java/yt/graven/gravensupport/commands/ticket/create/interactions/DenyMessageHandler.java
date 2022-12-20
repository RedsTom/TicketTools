package yt.graven.gravensupport.commands.ticket.create.interactions;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Emoji;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.springframework.stereotype.Component;
import yt.graven.gravensupport.utils.interactions.IInteractionAction;

@Component
public class DenyMessageHandler implements IInteractionAction<ButtonInteractionEvent> {

  @Override
  public void run(ButtonInteractionEvent event) {
    List<MessageEmbed> embedList = new ArrayList<>(event.getMessage().getEmbeds());
    embedList.add(new EmbedBuilder().setTitle("Envoi annulé").setColor(Color.RED).build());
    event
        .deferEdit()
        .setActionRow(Button.secondary("delete", Emoji.fromUnicode("🗑️")))
        .setEmbeds(embedList)
        .queue();
  }
}
