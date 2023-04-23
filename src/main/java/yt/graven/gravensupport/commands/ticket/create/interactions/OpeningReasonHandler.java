package yt.graven.gravensupport.commands.ticket.create.interactions;

import java.awt.*;
import java.io.IOException;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;
import org.springframework.stereotype.Component;
import yt.graven.gravensupport.commands.ticket.Ticket;
import yt.graven.gravensupport.commands.ticket.TicketManager;
import yt.graven.gravensupport.commands.ticket.TicketOpeningReason;
import yt.graven.gravensupport.utils.exceptions.TicketException;
import yt.graven.gravensupport.utils.interactions.InteractionAction;
import yt.graven.gravensupport.utils.messages.Embeds;
import yt.graven.gravensupport.utils.messages.builder.MessageFactory;

@Component
@RequiredArgsConstructor
public class OpeningReasonHandler implements InteractionAction<StringSelectInteractionEvent> {

    private final Embeds embeds;
    private final TicketManager manager;

    @Override
    public void run(StringSelectInteractionEvent event) throws TicketException, IOException {
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

        SelectOption selectedOption =
                event.getInteraction().getSelectedOptions().get(0);

        if (selectedOption.getValue().equalsIgnoreCase("op-other")) {
            Modal modal = Modal.create("op-other-reason", "Pourquoi ouvrez-vous un ticket ?")
                    .addActionRows(
                            ActionRow.of(TextInput.create("reason", "Raison (en quelques mots)", TextInputStyle.SHORT)
                                    .build()))
                    .build();

            event.replyModal(modal).queue();

            return;
        }

        if (selectedOption.getValue().equalsIgnoreCase("op-user-report")) {
            // spotless:off
            MessageFactory.create()
                    .addEmbeds(new EmbedBuilder()
                            .setTitle(":warning: Vous êtes sur le point de transmettre un signalement à la modération.")
                            .setDescription("""
                                    Avant de continuer, merci de vous assurer que vous avez en votre possession l'identifiant de l'utilisateur que vous souhaitez signaler.
                                    :pencil: *Si vous ne savez pas comment le récupérer, merci de suivre les instructions indiquées sur [ce lien](<https://support.discord.com/hc/en-us/articles/206346498-Where-can-I-find-my-User-Server-Message-ID->)*

                                    Une fois que vous avez cette information en votre possession, vous pouvez cliquer sur le bouton ci-dessous :arrow_down:
                                    """)
                            .setColor(Color.GREEN))
                    .addActionRow(actionRow -> actionRow
                            .addButton("report-user", button ->
                                    button.setText("Continuer")
                            )
                    )
                    .reply(event)
                    .queue();
            // spotless:on

            return;
        }

        InteractionHook reply = event.deferReply(true).complete();
        ticket.get().openOnServer(false, null, new TicketOpeningReason.Simple(selectedOption.getLabel()));
        reply.editOriginalEmbeds(new EmbedBuilder()
                        .setColor(Color.GREEN)
                        .setTitle("Ticket ouvert !")
                        .setDescription(
                                "Le ticket a bien été ouvert ! Vous pouvez désormais communiquer avec la modération")
                        .build())
                .queue();
    }
}
