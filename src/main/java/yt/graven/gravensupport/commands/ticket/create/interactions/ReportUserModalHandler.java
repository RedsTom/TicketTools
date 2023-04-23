package yt.graven.gravensupport.commands.ticket.create.interactions;

import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import org.springframework.stereotype.Component;
import yt.graven.gravensupport.commands.ticket.Ticket;
import yt.graven.gravensupport.commands.ticket.TicketManager;
import yt.graven.gravensupport.commands.ticket.TicketOpeningReason;
import yt.graven.gravensupport.utils.exceptions.TicketException;
import yt.graven.gravensupport.utils.interactions.InteractionAction;
import yt.graven.gravensupport.utils.messages.Embeds;

import java.awt.*;
import java.io.IOException;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ReportUserModalHandler implements InteractionAction<ModalInteractionEvent> {

    private final TicketManager manager;
    private final Embeds embeds;

    @Override
    public void run(ModalInteractionEvent event) throws TicketException, IOException {

        InteractionHook reply = event.deferReply(true).complete();

        String userId = event.getInteraction().getValue("user-id").getAsString();
        String reason = event.getInteraction().getValue("reason").getAsString();

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

        ticket.get().openOnServer(false, null, new TicketOpeningReason.UserReport(userId, reason));
        reply.editOriginalEmbeds(new EmbedBuilder()
                        .setColor(Color.GREEN)
                        .setTitle("Ticket ouvert !")
                        .setDescription("""
                                Le ticket a bien été ouvert ! Vous pouvez désormais communiquer avec la modération.
                                
                                Si vous avez des preuves pour étayer votre signalement, vous pouvez désormais les envoyer.
                                """)
                        .build())
                .queue();

    }
}
