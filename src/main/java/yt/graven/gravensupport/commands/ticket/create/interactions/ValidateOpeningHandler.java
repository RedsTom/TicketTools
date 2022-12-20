package yt.graven.gravensupport.commands.ticket.create.interactions;

import java.awt.*;
import java.io.IOException;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.PrivateChannel;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import org.springframework.stereotype.Component;
import yt.graven.gravensupport.commands.ticket.Ticket;
import yt.graven.gravensupport.commands.ticket.TicketManager;
import yt.graven.gravensupport.utils.exceptions.TicketException;
import yt.graven.gravensupport.utils.interactions.IInteractionAction;
import yt.graven.gravensupport.utils.messages.Embeds;

@Component
@RequiredArgsConstructor
public class ValidateOpeningHandler implements IInteractionAction<ButtonInteractionEvent> {

  private final TicketManager manager;
  private final Embeds embeds;

  @Override
  public void run(ButtonInteractionEvent event) throws TicketException, IOException {
    if (event.getChannel().getType() != ChannelType.PRIVATE) return;

    PrivateChannel channel = event.getPrivateChannel();
    Optional<Ticket> ticket = manager.get(channel.getUser());

    if (ticket.isEmpty()) {
      ticket = Optional.of(manager.create(channel.getUser()));
    }

    if (ticket.get().isOpened()) {
      event.deferReply(true).addEmbeds(embeds.ticketAlreadyExists(true).build()).queue();
      return;
    }

    ticket.get().openOnServer(false, null, "");
    event
        .deferReply(true)
        .addEmbeds(
            new EmbedBuilder()
                .setColor(Color.GREEN)
                .setTitle("Ticket ouvert !")
                .setDescription(
                    "Le ticket a bien été ouvert ! Vous pouvez désormais communiquer avec la modération")
                .build())
        .queue();
  }
}
