package me.redstom.tickettools.commands.ticket.interactions;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import me.redstom.tickettools.utils.interactions.InteractionAction;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.springframework.stereotype.Component;

@Component
public class DenyMessageHandler implements InteractionAction<ButtonInteractionEvent> {

    @Override
    public void run(ButtonInteractionEvent event) {
        List<MessageEmbed> embedList = new ArrayList<>(event.getMessage().getEmbeds());
        MessageEmbed embed = new EmbedBuilder()
                .setTitle("Envoi annulé")
                .setColor(Color.RED)
                .setFooter(
                        "Annulé par " + event.getUser().getAsTag(),
                        event.getUser().getAvatarUrl())
                .build();
        embedList.add(embed);
        event.deferEdit()
                .setActionRow(Button.secondary("delete", Emoji.fromUnicode("🗑️")))
                .setEmbeds(embedList)
                .queue();
    }
}
