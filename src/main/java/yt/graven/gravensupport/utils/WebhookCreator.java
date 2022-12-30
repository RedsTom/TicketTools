package yt.graven.gravensupport.utils;

import club.minnced.discord.webhook.send.AllowedMentions;
import club.minnced.discord.webhook.send.WebhookEmbedBuilder;
import club.minnced.discord.webhook.send.WebhookMessageBuilder;
import java.util.EnumSet;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.dv8tion.jda.api.entities.IMentionable;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.internal.entities.DataMessage;
import net.dv8tion.jda.internal.entities.ReceivedMessage;
import org.jetbrains.annotations.NotNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class WebhookCreator {
  @NotNull public static WebhookMessageBuilder fromJDA(
      @NotNull net.dv8tion.jda.api.entities.Message message) {
    WebhookMessageBuilder builder = new WebhookMessageBuilder();
    builder.setTTS(message.isTTS());
    builder.setContent(message.getContentRaw());
    message
        .getEmbeds()
        .forEach(embed -> builder.addEmbeds(WebhookEmbedBuilder.fromJDA(embed).build()));

    if (message instanceof DataMessage) {
      DataMessage data = (DataMessage) message;
      AllowedMentions allowedMentions = AllowedMentions.none();
      EnumSet<Message.MentionType> parse = data.getAllowedMentions();
      allowedMentions.withUsers(data.getMentionedUsersWhitelist());
      allowedMentions.withRoles(data.getMentionedRolesWhitelist());
      if (parse != null) {
        allowedMentions.withParseUsers(parse.contains(Message.MentionType.USER));
        allowedMentions.withParseRoles(parse.contains(Message.MentionType.ROLE));
        allowedMentions.withParseEveryone(
            parse.contains(Message.MentionType.EVERYONE)
                || parse.contains(Message.MentionType.HERE));
      }
      builder.setAllowedMentions(allowedMentions);
    } else if (message instanceof ReceivedMessage) {
      AllowedMentions allowedMentions = AllowedMentions.none();
      allowedMentions.withRoles(
          message.getMentions().getMentions(Message.MentionType.ROLE).stream()
              .map(IMentionable::getId)
              .collect(Collectors.toList()));
      allowedMentions.withUsers(
          message.getMentions().getMentions(Message.MentionType.USER).stream()
              .map(IMentionable::getId)
              .collect(Collectors.toList()));
      allowedMentions.withParseEveryone(message.getMentions().mentionsEveryone());
      builder.setAllowedMentions(allowedMentions);
      builder.setEphemeral(message.isEphemeral());
    }
    return builder;
  }
}
