package yt.graven.gravensupport.commands.ticket.interactions;

import java.awt.*;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.utils.MiscUtil;
import org.springframework.stereotype.Component;
import yt.graven.gravensupport.commands.ticket.Ticket;
import yt.graven.gravensupport.commands.ticket.TicketManager;
import yt.graven.gravensupport.utils.interactions.InteractionAction;
import yt.graven.gravensupport.utils.messages.Embeds;
import yt.graven.gravensupport.utils.messages.builder.MessageFactory;

@Component
@RequiredArgsConstructor
public class FirstSentenceHandler implements InteractionAction<StringSelectInteractionEvent> {

    private final TicketManager ticketManager;
    private final Embeds embeds;

    @Override
    public void run(StringSelectInteractionEvent event) {
        Optional<Ticket> ticket = ticketManager.get(MiscUtil.parseLong(((TextChannel) event.getChannel()).getTopic()));
        if (ticket.isEmpty()) {
            event.deferReply(true)
                    .addEmbeds(new EmbedBuilder()
                            .setColor(Color.RED)
                            .setTitle("Erreur")
                            .setDescription("Impossible de trouver le ticket associé à ce salon !")
                            .setFooter("")
                            .build())
                    .queue();
            return;
        }

        String content =
                switch (event.getInteraction().getSelectedOptions().get(0).getValue()) {
                    case "bonsoir" -> ":wave: Bonsoir, comment pouvons-nous vous aider ?";
                    case "bonjour" -> ":wave: Bonjour, comment pouvons-nous vous aider ?";
                    default -> "";
                };

        if (content.isEmpty()) return;

        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("Message transmis :")
                .setDescription(content)
                .setFooter("")
                .setColor(Color.GREEN);

        MessageFactory.create()
                .setTextContent(content)
                .send(ticket.get().getFrom())
                .queue(
                        msg -> {
                            MessageFactory.create()
                                    .addEmbeds(embed.build())
                                    .send(event.getChannel())
                                    .queue();
                            event.getMessage().delete().queue();
                        },
                        err -> event.reply(embeds.errorMessage(err.getMessage()).build())
                                .queue());
    }
}
