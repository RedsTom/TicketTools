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
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.utils.MiscUtil;
import org.springframework.stereotype.Component;

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

        try {
            MiscUtil.parseSnowflake(userId);
        } catch (Exception e) {
            reply.editOriginalEmbeds(embeds.error("L'identifiant de l'utilisateur fourni est invalide !")
                            .build())
                    .queue();
            return;
        }

        PrivateChannel channel = event.getChannel().asPrivateChannel();
        Optional<Ticket> ticket = manager.get(channel.getUser());

        if (ticket.isEmpty()) {
            ticket = Optional.of(manager.create(channel.getUser()));
        }

        Ticket sureTicket = ticket.get();
        if (sureTicket.isOpened()) {
            reply.editOriginalEmbeds(embeds.ticketAlreadyExists(true).build()).queue();
            return;
        }

        sureTicket.openOnServer(false, null, new TicketOpeningReason.UserReport(userId, reason));

        reply.editOriginalEmbeds(new EmbedBuilder()
                        .setColor(Color.GREEN)
                        .setTitle("Ticket ouvert !")
                        .setDescription(
                                """
                                        Le ticket a bien été ouvert ! Vous pouvez désormais communiquer avec la modération.

                                        **Rappel :**
                                        Utilisateur signalé : %s (<@%s>)
                                        Raison : %s

                                        Si vous avez des preuves pour étayer votre signalement, vous pouvez désormais les envoyer.
                                        """
                                        .formatted(userId, userId, reason))
                        .build())
                .queue();
    }
}
