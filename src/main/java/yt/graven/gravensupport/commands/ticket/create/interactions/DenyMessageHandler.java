package yt.graven.gravensupport.commands.ticket.create.interactions;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Emoji;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.interactions.components.Button;
import org.springframework.beans.factory.annotation.Autowired;
import yt.graven.gravensupport.commands.ticket.TicketManager;
import yt.graven.gravensupport.utils.interactions.IIInteractionAction;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class DenyMessageHandler implements IIInteractionAction<ButtonClickEvent> {

    @Autowired
    private TicketManager ticketManager;

    @Override
    public void run(ButtonClickEvent event) {
        List<MessageEmbed> embedList = new ArrayList<>(event.getMessage().getEmbeds());
        embedList.add(new EmbedBuilder()
            .setTitle("Envoi annulé")
            .setColor(Color.RED)
            .build());
        event.deferEdit()
            .setActionRow(Button.secondary("delete", Emoji.fromUnicode("🗑️")))
            .setEmbeds(embedList)
            .queue();
    }
}
