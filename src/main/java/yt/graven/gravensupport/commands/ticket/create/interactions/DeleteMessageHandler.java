package yt.graven.gravensupport.commands.ticket.create.interactions;

import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.utils.MiscUtil;
import org.springframework.stereotype.Component;
import yt.graven.gravensupport.commands.ticket.OldTicket;
import yt.graven.gravensupport.commands.ticket.OldTicketManager;
import yt.graven.gravensupport.utils.interactions.IIInteractionAction;
import yt.graven.gravensupport.utils.messages.builder.MessageFactory;
import yt.graven.gravensupport.utils.messages.builder.data.TicketActionRow;

import java.awt.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class DeleteMessageHandler implements IIInteractionAction<ButtonInteractionEvent> {

    private final OldTicketManager oldTicketManager;

    @Override
    public void run(ButtonInteractionEvent event) {
        Message embedMessage = event.getMessage();
        MessageEmbed baseEmbed = embedMessage.getEmbeds().get(0);

        Optional<OldTicket> ticket = oldTicketManager.get(MiscUtil.parseLong(((TextChannel) event.getChannel()).getTopic()));
        if (ticket.isEmpty()) {
            MessageEmbed embed = new EmbedBuilder()
                    .setColor(Color.RED)
                    .setTitle("Erreur")
                    .setDescription("Impossible de trouver le ticket associé à ce salon !")
                    .setFooter("")
                    .build();

            event.deferReply(true).addEmbeds(embed).queue();
            return;
        }

        long messageId = MiscUtil.parseLong(
                baseEmbed.getFields().get(baseEmbed.getFields().size() - 1).getValue());

        Message referingMessage = ticket.get()
                .getFrom()
                .openPrivateChannel()
                .complete()
                .getHistoryAround(messageId, 50)
                .complete()
                .getMessageById(messageId);

        if (referingMessage == null) {
            MessageEmbed embed = new EmbedBuilder()
                    .setColor(Color.RED)
                    .setTitle("Erreur")
                    .setDescription("Impossible de trouver le message cible associé à cet envoi !")
                    .build();

            event.deferReply(true).addEmbeds(embed).queue();
            return;
        }

        referingMessage.delete().queue();

        List<MessageEmbed> embedList = new ArrayList<>(event.getMessage().getEmbeds());
        MessageEmbed embed = new EmbedBuilder()
                .setTitle("Message supprimé")
                .setColor(Color.RED)
                .setTimestamp(Instant.now())
                .build();
        embedList.add(embed);

        MessageFactory.create()
                .addEmbeds(embedList.toArray(MessageEmbed[]::new))
                .addActionRow(TicketActionRow::addDeleteButton)
                .editReply(event.deferEdit().complete());
    }
}
