package yt.graven.gravensupport.commands.ticket.create.interactions;

import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import org.springframework.stereotype.Component;
import yt.graven.gravensupport.commands.ticket.Ticket;
import yt.graven.gravensupport.commands.ticket.TicketManager;
import yt.graven.gravensupport.utils.exceptions.TicketException;
import yt.graven.gravensupport.utils.interactions.InteractionAction;
import yt.graven.gravensupport.utils.messages.Embeds;
import yt.graven.gravensupport.utils.messages.builder.MessageFactory;
import yt.graven.gravensupport.utils.messages.builder.data.TicketMessage;

import java.awt.*;
import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OpenWithReportedHandler implements InteractionAction<ButtonInteractionEvent> {

    private final TicketManager manager;
    private final JDA jda;
    private final Embeds embeds;

    @Override
    public void run(ButtonInteractionEvent event) throws TicketException, IOException {

        String userId = event.getButton().getId().split(";")[1];
        User user = jda.retrieveUserById(userId).complete();

        if (manager.exists(user)) {
            embeds.errorMessage("Un ticket avec cet utilisateur existe déjà !")
                    .reply(event)
                    .queue();

            return;
        }

        Ticket ticket = manager.create(user);
        ticket.forceOpening(event.getUser());

        MessageFactory.create()
                .addEmbeds(new EmbedBuilder()
                        .setColor(Color.CYAN)
                        .setTitle("Ticket de signalement")
                        .setDescription("Ce ticket a été ouvert suite à un signalement de %s (%s)".formatted(
                                event.getUser().getAsMention(),
                                event.getChannel().getAsMention()
                        ))
                )
                .send(ticket.getTo())
                .complete()
                .pin()
                .queue();

        event.getMessage()
                .editMessageComponents(ActionRow.of(
                        Button.of(ButtonStyle.LINK, ticket.getTo().getJumpUrl(), "Accéder au ticket de l'utilisateur signalé")
                ))
                .queue();

        embeds.successMessage("Le ticket a été ouvert avec succès !\n:arrow_right: %s".formatted(ticket.getTo().getAsMention()))
                .reply(event)
                .queue();

    }
}
