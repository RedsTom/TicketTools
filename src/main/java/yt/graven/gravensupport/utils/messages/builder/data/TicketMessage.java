package yt.graven.gravensupport.utils.messages.builder.data;

import java.util.Arrays;
import java.util.function.UnaryOperator;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;
import net.dv8tion.jda.api.requests.restaction.MessageEditAction;
import net.dv8tion.jda.api.requests.restaction.WebhookMessageEditAction;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import net.dv8tion.jda.api.utils.messages.MessageEditData;

public class TicketMessage {

    private final MessageCreateBuilder builder;

    public TicketMessage() {
        this.builder = new MessageCreateBuilder();
    }

    public TicketMessage(MessageCreateBuilder builder) {
        this.builder = builder;
    }

    public TicketMessage addEmbeds(MessageEmbed... embeds) {
        builder.addEmbeds(embeds);
        return this;
    }

    public TicketMessage addEmbeds(EmbedBuilder... embeds) {
        MessageEmbed[] built = Arrays.stream(embeds).map(EmbedBuilder::build).toArray(MessageEmbed[]::new);

        return addEmbeds(built);
    }

    public TicketMessage setTextContent(String content) {
        builder.setContent(content);
        return this;
    }

    public TicketMessage apply(UnaryOperator<MessageCreateBuilder> applier) {
        applier.apply(builder);
        return this;
    }

    public TicketMessage addActionRow(UnaryOperator<TicketActionRow> actionRow) {
        TicketActionRow row = new TicketActionRow();
        actionRow.apply(row);

        row.build().ifPresent(builder::addComponents);
        return this;
    }

    public MessageCreateAction send(User user) {
        PrivateChannel channel = user.openPrivateChannel().complete();
        return send(channel);
    }

    public MessageCreateAction send(MessageChannel channel) {
        return channel.sendMessage(builder.build());
    }

    public ReplyCallbackAction reply(SlashCommandInteractionEvent event) {
        return event.deferReply(true).applyData(builder.build());
    }

    public ReplyCallbackAction reply(StringSelectInteractionEvent event) {
        return event.deferReply(true).applyData(builder.build());
    }

    public WebhookMessageEditAction<Message> editReply(InteractionHook hook) {
        MessageEditData editData = MessageEditData.fromCreateData(builder.build());
        return hook.editOriginal(editData);
    }

    public MessageEditAction edit(Message message) {
        MessageEditData editData = MessageEditData.fromCreateData(builder.build());
        return message.editMessage(editData);
    }

    public MessageCreateData build() {
        return builder.build();
    }
}
