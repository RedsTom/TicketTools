package yt.graven.gravensupport.utils.messages.builder;

import java.util.Arrays;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import yt.graven.gravensupport.utils.messages.builder.data.TicketMessage;

public class MessageFactory {

    private MessageFactory() {}

    public static TicketMessage create() {
        return new TicketMessage();
    }

    public static TicketMessage from(MessageCreateBuilder builder) {
        return new TicketMessage(builder);
    }
}
