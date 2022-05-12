package yt.graven.gravensupport.commands.ticket.create.interactions;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.PrivateChannel;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import yt.graven.gravensupport.commands.ticket.Ticket;
import yt.graven.gravensupport.commands.ticket.TicketManager;
import yt.graven.gravensupport.utils.exceptions.TicketException;
import yt.graven.gravensupport.utils.interactions.IIInteractionAction;
import yt.graven.gravensupport.utils.messages.Embeds;

import java.awt.*;
import java.io.IOException;
import java.util.Optional;

@Component
public class ValidateOpeningHandler implements IIInteractionAction<ButtonClickEvent> {

    @Autowired
    private TicketManager manager;

    @Autowired
    private Embeds embeds;

    @Override
    public void run(ButtonClickEvent event) throws TicketException, IOException {
        if (event.getChannel().getType() != ChannelType.PRIVATE) return;

        PrivateChannel channel = event.getPrivateChannel();
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

        ticket.get().openOnServer(false, null);
        event.deferReply(true)
            .addEmbeds(new EmbedBuilder()
                .setColor(Color.GREEN)
                .setTitle("Ticket ouvert !")
                .setDescription("Le ticket a bien été ouvert ! Vous pouvez désormais communiquer avec la modération")
                .build())
            .queue();
    }
}
