package yt.graven.gravensupport.commands.ticket;

import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.external.JDAWebhookClient;
import club.minnced.discord.webhook.send.WebhookMessageBuilder;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.awt.*;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.requests.ErrorResponse;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import org.simpleyaml.configuration.file.YamlConfiguration;
import yt.graven.gravensupport.utils.WebhookMessageAdapter;
import yt.graven.gravensupport.utils.exceptions.TicketAlreadyExistsException;
import yt.graven.gravensupport.utils.messages.Embeds;
import yt.graven.gravensupport.utils.messages.TMessage;
import yt.graven.gravensupport.utils.messages.serializable.SerializableMessageArray;

public class Ticket {

  private final TicketManager ticketManager;
  private final Embeds embeds;
  private final YamlConfiguration config;
  private final User from;
  private final Guild moderationGuild;
  private final Emoji sentEmote;
  private TextChannel to;
  private boolean opened;

  private static final Gson GSON =
      new GsonBuilder().setPrettyPrinting().excludeFieldsWithoutExposeAnnotation().create();
  private WebhookClient webhook;

  private Ticket(
      TicketManager ticketManager,
      Embeds embeds,
      YamlConfiguration config,
      User from,
      TextChannel to,
      WebhookClient webhook) {
    this.embeds = embeds;
    this.from = from;
    this.to = to;
    this.config = config;
    this.opened = true;
    this.ticketManager = ticketManager;
    this.webhook = webhook;

    this.moderationGuild =
        from.getJDA().getGuildById(config.getString("config.ticket_guild.guild_id"));
    this.sentEmote =
        this.moderationGuild
            .retrieveEmojiById(this.config.getString("config.ticket_guild.reaction_id"))
            .complete();
  }

  /**
   * Creates a new ticket
   *
   * @param from User to create the ticket with
   */
  public Ticket(TicketManager ticketManager, Embeds embeds, YamlConfiguration config, User from) {
    this(ticketManager, embeds, config, from, null, null);
    this.opened = false;
  }

  public static Ticket loadFromChannel(
      TicketManager ticketManager, Embeds embeds, YamlConfiguration config, TextChannel channel)
      throws IOException {
    String topic = channel.getTopic();
    topic = topic == null ? "0" : topic;

    User user = channel.getJDA().retrieveUserById(topic).complete();
    if (user == null) {
      throw new RuntimeException(
          "Error : Unable to find an user matching the ticket #" + channel.getName() + " !");
    }

    Ticket ticket = new Ticket(ticketManager, embeds, config, user, channel, null);
    ticket.webhook = ticket.retrieveWebhook();
    return ticket;
  }

  /** Sends a message to confirm the opening of the ticket in the case of a user-opened ticket. */
  public void proposeOpening(InteractionHook reply) {
    if ((webhook != null && to != null) || opened) {
      throw new TicketAlreadyExistsException(from);
    }

    ErrorHandler errorHandler =
        new ErrorHandler()
            .ignore(ErrorResponse.UNKNOWN_MESSAGE)
            .handle(ErrorResponse.CANNOT_SEND_TO_USER, exception -> handleUnableToDmUser(reply));

    from.openPrivateChannel()
        .complete()
        .sendMessage(
            TMessage.from(embeds.proposeOpening(sentEmote.getFormatted()))
                .actionRow()
                .add(Button.secondary("?", "Raison : ").asDisabled())
                .build()
                .actionRow()
                .selectMenu("opening-reason")
                .addOption(
                    "Signalement utilisateur", "op-user-report", Emoji.fromUnicode("\uD83D\uDCDD"))
                .addOption("Contester une sanction", "op-unban", Emoji.fromUnicode("⛔"))
                .addOption("Proposer une amélioration", "op-enhancement", Emoji.fromUnicode("✨"))
                .addOption("Autre", "op-other", Emoji.fromUnicode("\uD83D\uDCAC"))
                .build()
                .build()
                .build())
        .queue(
            msg -> {
              reply.editOriginal("➡️ " + msg.getChannel().getAsMention()).queue();
            },
            errorHandler);
  }

  /** Directly opens a ticket without asking for the user permission. */
  public void forceOpening(User by) throws IOException {
    TMessage.from(embeds.forceOpening(sentEmote.getFormatted())).sendMessage(from).queue();

    openOnServer(true, by, null);
  }

  public void openOnServer(boolean forced, User by, String reason) throws IOException {
    if (opened) {
      throw new TicketAlreadyExistsException(from);
    }

    Category category =
        moderationGuild.getCategoryById(config.getString("config.ticket_guild.tickets_category"));
    TextChannel channel =
        category.createTextChannel(from.getName()).setTopic(from.getId()).complete();
    this.to = channel;
    this.webhook = retrieveWebhook();

    if (!forced) {
      TMessage.create()
          .setEmbeds(
              new EmbedBuilder()
                  .setTitle("\uD83D\uDCDD Raison de l'ouverture du ticket")
                  .setDescription("**`" + reason + "`**")
                  .setColor(0x48dbfb)
                  .build())
          .sendMessage(channel)
          .complete();
      TMessage.create()
          .setEmbeds(
              new EmbedBuilder()
                  .setTitle("Sélectionnez le premier message à envoyer :")
                  .setColor(0x48dbfb)
                  .build())
          .actionRow()
          .selectMenu("first-sentence")
          .addOption("Bonjour", "bonjour", Emoji.fromUnicode("☀️"))
          .addOption("Bonsoir", "bonsoir", Emoji.fromUnicode("🌙"))
          .build()
          .build()
          .actionRow()
          .deletable()
          .build()
          .sendMessage(channel)
          .complete();
    }

    TextChannel ticketChannel =
        moderationGuild.getTextChannelById(
            config.getString("config.ticket_guild.channels_ids.tickets"));
    TMessage.from(embeds.ticketOpening(forced, by, from, channel, reason))
        .actionRow()
        .button()
        .withText("Aller au salon")
        .withLink(
            String.format(
                "https://discord.com/channels/%s/%s", moderationGuild.getId(), channel.getId()))
        .build()
        .button()
        .withText("Aller à l'utilisateur")
        .withLink(String.format("https://discord.com/users/%s", from.getId()))
        .build()
        .build()
        .sendMessage(ticketChannel)
        .queue();

    opened = true;
  }

  private WebhookClient retrieveWebhook() throws IOException {
    List<Webhook> webhooks = to.retrieveWebhooks().complete();
    return JDAWebhookClient.from(
        switch (webhooks.size()) {
          case 0 -> to.createWebhook(from.getName())
              .setAvatar(Icon.from(new URL(from.getEffectiveAvatarUrl()).openStream()))
              .complete();
          default -> webhooks.get(0);
        });
  }

  public TextChannel getTo() {
    return to;
  }

  public User getFrom() {
    return from;
  }

  public boolean isOpened() {
    return opened;
  }

  public void sendToTicket(Message message) {
    Executors.newSingleThreadExecutor()
        .execute(
            () -> {
              WebhookMessageBuilder builder = WebhookMessageAdapter.fromJDA(message);
              webhook
                  .send(builder.build())
                  .thenAccept(
                      msg -> {
                        message.addReaction(sentEmote).queue();
                      })
                  .exceptionally(
                      (error) -> {
                        message.addReaction(Emoji.fromUnicode("❌")).queue();
                        return null;
                      });
            });
  }

  public void sendToUser(Message message) {
    String content = message.getContentRaw().substring(1).trim();

    EmbedBuilder embedBuilder =
        new EmbedBuilder()
            .setTitle("Confirmer l'envoi du message ?")
            .setDescription(content)
            .setColor(Color.ORANGE)
            .setFooter(
                "⚠️ Tant que l'envoi du message n'a pas été confirmé, vous pouvez éditer son contenu.")
            .addField(
                "🔗 Identifiant du message",
                String.format("[%s](%s)", "" + message.getId(), "" + message.getJumpUrl()),
                true);

    if (message.getAttachments().size() != 0) {
      embedBuilder.addField(
          "📎 Pièces jointes :",
          "`"
              + message.getAttachments().stream()
                  .map(Message.Attachment::getFileName)
                  .collect(Collectors.joining("`, `"))
              + "`",
          true);
    }

    TMessage.from(embedBuilder.build())
        .actionRow()
        .button("confirm-message")
        .withStyle(ButtonStyle.SUCCESS)
        .withText("Confirmer")
        .build()
        .button("deny-message")
        .withStyle(ButtonStyle.DANGER)
        .withText("Annuler")
        .build()
        .deletable()
        .build()
        .sendMessage(message.getChannel())
        .queue();
  }

  public CompletableFuture<Message> confirmSendToUser(MessageCreateData message) {
    CompletableFuture<Message> cf = new CompletableFuture<>();

    Executors.newSingleThreadExecutor()
        .execute(
            () -> {
              try {
                TMessage builder =
                    TMessage.from(MessageCreateBuilder.from(message))
                        .setContent(
                            message.getContent().startsWith("'")
                                ? message.getContent().substring(1).trim()
                                : message.getContent().trim());

                try {
                  // Set attachements as files in builder
                  Set<String> passed = new HashSet<>();
                  UnaryOperator<String> nameGenerator =
                      (name) -> {
                        if (passed.contains(name)) {
                          return name + "-" + UUID.randomUUID();
                        } else {
                          passed.add(name);
                          return name;
                        }
                      };
                  builder.addFiles(
                      message.getAttachments().stream()
                          .map(
                              a ->
                                  FileUpload.fromData(
                                      a.getData(), nameGenerator.apply(a.getName())))
                          .toArray(FileUpload[]::new));
                } catch (UnsupportedOperationException ignored) {
                }

                builder
                    .sendMessage(getFrom())
                    .queue(
                        cf::complete,
                        (e) -> {
                          getTo().sendMessage(embeds.errorMessage(e.getMessage()).build()).queue();
                        });
              } catch (Exception e) {
                cf.completeExceptionally(e);
              }
            });

    return cf;
  }

  public void close() {
    Executors.newSingleThreadExecutor()
        .execute(
            () -> {
              List<Message> messages = null;
              try {
                messages = to.getIterableHistory().takeWhileAsync(Objects::nonNull).get();
              } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
              }
              Collections.reverse(messages);

              SerializableMessageArray sma =
                  new SerializableMessageArray(
                      config.getString("config.ticket_guild.channels_ids.attachements"), from);

              messages.forEach(sma::addMessage);

              String json = GSON.toJson(sma);
              TextChannel reportsChannel =
                  moderationGuild.getTextChannelById(
                      config.getString("config.ticket_guild.channels_ids.reports"));
              Message report =
                  reportsChannel
                      .sendMessage("Rapport du ticket de `@" + from.getAsTag() + "`")
                      .addFiles(
                          FileUpload.fromData(
                              json.getBytes(StandardCharsets.UTF_8), to.getName() + ".json"))
                      .complete();

              String reportJsonUrl = "";
              for (Message.Attachment attachment : report.getAttachments()) {
                reportJsonUrl = attachment.getUrl();
              }

              TextChannel ticketsChannel =
                  moderationGuild.getTextChannelById(
                      config.getString("config.ticket_guild.channels_ids.tickets"));
              TMessage.from(embeds.ticketClosing(from, report.getJumpUrl()))
                  .actionRow()
                  .button()
                  .withText("Aller au rapport")
                  .withLink(report.getJumpUrl())
                  .build()
                  .button()
                  .withText("Consulter le rapport (en ligne)")
                  .withLink(
                      "https://redstom.github.io/GravenDev-TicketReader/?input=" + reportJsonUrl)
                  .build()
                  .build()
                  .sendMessage(ticketsChannel)
                  .complete();

              String ticket =
                  from.getJDA().retrieveCommands().complete().stream()
                      .filter(a -> a.getName().equalsIgnoreCase("ticket"))
                      .findFirst()
                      .map(a -> a.getAsMention())
                      .orElse("`/ticket`");

              TMessage.create()
                  .setEmbeds(
                      new EmbedBuilder()
                          .setColor(Color.RED)
                          .setTitle("Ticket fermé.")
                          .setDescription(
                              "La modération a fermé le ticket avec vous. Si vous souhaitez le rouvrir, "
                                  + "refaites la commande "
                                  + ticket
                                  + "."))
                  .sendMessage(from)
                  .queue();

              to.delete().queue((v1) -> ticketManager.remove(from));
            });
  }

  private void handleUnableToDmUser(InteractionHook reply) {
    ticketManager.remove(from);

    String errorMessage = "Impossible d'envoyer un message privé à cet utilisateur!";
    MessageEmbed embed = embeds.error(errorMessage).build();

    TMessage.from(embed).editReply(reply).queue();
  }
}
