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

    /**
     * @deprecated Use {@link TicketMessage#addEmbeds(MessageEmbed...)} )} instead
     */
    @Deprecated(forRemoval = true)
    public static TicketMessage from(MessageEmbed... embeds) {
        return create().apply(builder -> builder.setEmbeds(embeds));
    }

    /**
     * @deprecated Use {@link TicketMessage#addEmbeds(EmbedBuilder...)} )} instead
     */
    @Deprecated(forRemoval = true)
    public static TicketMessage from(EmbedBuilder... content) {
        MessageEmbed[] embeds = Arrays.stream(content).map(EmbedBuilder::build).toArray(MessageEmbed[]::new);

        return from(embeds);
    }
}
