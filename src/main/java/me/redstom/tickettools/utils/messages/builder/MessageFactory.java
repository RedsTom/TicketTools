package me.redstom.tickettools.utils.messages.builder;

import me.redstom.tickettools.utils.messages.builder.data.TicketMessage;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;

public class MessageFactory {

    private MessageFactory() {}

    public static TicketMessage create() {
        return new TicketMessage();
    }

    public static TicketMessage from(MessageCreateBuilder builder) {
        return new TicketMessage(builder);
    }
}
