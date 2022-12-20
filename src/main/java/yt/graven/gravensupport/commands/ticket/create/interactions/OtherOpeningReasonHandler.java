package yt.graven.gravensupport.commands.ticket.create.interactions;

import java.awt.*;
import java.io.IOException;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.PrivateChannel;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import org.springframework.stereotype.Component;
import yt.graven.gravensupport.commands.ticket.Ticket;
import yt.graven.gravensupport.commands.ticket.TicketManager;
import yt.graven.gravensupport.utils.exceptions.TicketException;
import yt.graven.gravensupport.utils.interactions.IInteractionAction;
import yt.graven.gravensupport.utils.messages.Embeds;

@Component
@RequiredArgsConstructor
public class OtherOpeningReasonHandler implements IInteractionAction<ModalInteractionEvent> {

  private final Embeds embeds;
  private final TicketManager manager;

  @Override
  public void run(ModalInteractionEvent event) throws TicketException, IOException {

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

    String reason = event.getValue("reason").getAsString();

    if (reason == null || reason.isEmpty()) {
      event
          .deferReply(true)
          .addEmbeds(embeds.error("Vous devez entrer une raison pour ouvrir un ticket.").build())
          .queue();
      return;
    }

    ticket.get().openOnServer(false, null, reason);
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
