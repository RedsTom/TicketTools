package me.redstom.tickettools.commands.ticket.interactions;

import java.awt.*;
import java.io.IOException;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import me.redstom.tickettools.commands.ticket.Ticket;
import me.redstom.tickettools.commands.ticket.TicketManager;
import me.redstom.tickettools.commands.ticket.TicketOpeningReason;
import me.redstom.tickettools.utils.exceptions.TicketException;
import me.redstom.tickettools.utils.interactions.InteractionAction;
import me.redstom.tickettools.utils.messages.Embeds;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ValidateOpeningHandler implements InteractionAction<ButtonInteractionEvent> {

    private final TicketManager manager;
    private final Embeds embeds;

    @Override
    public void run(ButtonInteractionEvent event) throws TicketException, IOException {
        if (event.getChannel().getType() != ChannelType.PRIVATE) return;

        PrivateChannel channel = event.getChannel().asPrivateChannel();
        Optional<Ticket> ticket = manager.get(channel.getUser());

        if (ticket.isEmpty()) {
            ticket = Optional.of(manager.create(channel.getUser()));
        }

        if (ticket.get().isOpened()) {
            event.deferReply(true)
                    .addEmbeds(embeds.ticketAlreadyExists(true).build())
                    .queue();
            return;
        }

        ticket.get().openOnServer(false, null, new TicketOpeningReason.Empty());
        event.deferReply(true)
                .addEmbeds(new EmbedBuilder()
                        .setColor(Color.GREEN)
                        .setTitle("Ticket ouvert !")
                        .setDescription(
                                "Le ticket a bien été ouvert ! Vous pouvez désormais communiquer avec la modération")
                        .build())
                .queue();
    }
}
