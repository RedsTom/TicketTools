package yt.graven.gravensupport.commands.ticket.create.interactions;

import java.awt.*;
import java.io.IOException;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.PrivateChannel;
import net.dv8tion.jda.api.events.interaction.component.SelectMenuInteractionEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Modal;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import org.springframework.stereotype.Component;
import yt.graven.gravensupport.commands.ticket.Ticket;
import yt.graven.gravensupport.commands.ticket.TicketManager;
import yt.graven.gravensupport.utils.exceptions.TicketException;
import yt.graven.gravensupport.utils.interactions.IInteractionAction;
import yt.graven.gravensupport.utils.messages.Embeds;

@Component
@RequiredArgsConstructor
public class OpeningReasonHandler implements IInteractionAction<SelectMenuInteractionEvent> {

  private final Embeds embeds;
  private final TicketManager manager;

  @Override
  public void run(SelectMenuInteractionEvent event) throws TicketException, IOException {
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

    SelectOption selectedOption = event.getInteraction().getSelectedOptions().get(0);

    if (selectedOption.getValue().equalsIgnoreCase("op-other")) {
      Modal modal =
          Modal.create("op-other-reason", "Pourquoi ouvrez-vous un ticket ?")
              .addActionRows(
                  ActionRow.of(
                      TextInput.create("reason", "Raison (en quelques mots)", TextInputStyle.SHORT)
                          .build()))
              .build();

      event.replyModal(modal).queue();

      return;
    }

    ticket.get().openOnServer(false, null, selectedOption.getLabel());
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
