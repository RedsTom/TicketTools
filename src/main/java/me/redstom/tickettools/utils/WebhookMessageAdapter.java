package me.redstom.tickettools.utils;

import club.minnced.discord.webhook.send.AllowedMentions;
import club.minnced.discord.webhook.send.WebhookEmbedBuilder;
import club.minnced.discord.webhook.send.WebhookMessageBuilder;
import java.util.EnumSet;
import java.util.stream.Collectors;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.utils.messages.MessageData;
import net.dv8tion.jda.internal.entities.ReceivedMessage;
import org.jetbrains.annotations.NotNull;

public class WebhookMessageAdapter {
    @NotNull public static WebhookMessageBuilder fromJDA(@NotNull Message message) {
        WebhookMessageBuilder builder = new WebhookMessageBuilder();
        builder.setTTS(message.isTTS());
        builder.setContent(message.getContentRaw());
        message.getEmbeds()
                .forEach(embed ->
                        builder.addEmbeds(WebhookEmbedBuilder.fromJDA(embed).build()));

        message.getAttachments()
                .forEach(attachment -> builder.addFile(
                        attachment.getFileName(),
                        attachment.getProxy().download().join()));

        if (message instanceof MessageData data) {
            AllowedMentions allowedMentions = AllowedMentions.none();
            EnumSet<Message.MentionType> parse = data.getAllowedMentions();
            if (parse != null) {
                allowedMentions.withParseUsers(parse.contains(Message.MentionType.USER));
                allowedMentions.withParseRoles(parse.contains(Message.MentionType.ROLE));
                allowedMentions.withParseEveryone(
                        parse.contains(Message.MentionType.EVERYONE) || parse.contains(Message.MentionType.HERE));
            }
            builder.setAllowedMentions(allowedMentions);
        } else if (message instanceof ReceivedMessage) {
            AllowedMentions allowedMentions = AllowedMentions.none();
            allowedMentions.withRoles(
                    message.getMentions().getRoles().stream().map(Role::getId).collect(Collectors.toList()));
            allowedMentions.withUsers(
                    message.getMentions().getUsers().stream().map(User::getId).collect(Collectors.toList()));
            allowedMentions.withParseEveryone(message.getMentions().mentionsEveryone());
            builder.setAllowedMentions(allowedMentions);
            builder.setEphemeral(message.isEphemeral());
        }
        return builder;
    }
}
