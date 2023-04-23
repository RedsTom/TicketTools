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
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.ICommandReference;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.requests.ErrorResponse;
import net.dv8tion.jda.api.utils.FileUpload;
import org.simpleyaml.configuration.file.YamlConfiguration;
import yt.graven.gravensupport.utils.WebhookMessageAdapter;
import yt.graven.gravensupport.utils.exceptions.TicketAlreadyExistsException;
import yt.graven.gravensupport.utils.messages.Embeds;
import yt.graven.gravensupport.utils.messages.builder.MessageFactory;
import yt.graven.gravensupport.utils.messages.builder.data.TicketActionRow;
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

    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .excludeFieldsWithoutExposeAnnotation()
            .create();
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

        this.moderationGuild = from.getJDA().getGuildById(config.getString("config.ticket_guild.guild_id"));
        this.sentEmote = this.moderationGuild
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

    /**
     * Sends a message to confirm the opening of the ticket in the case of a user-opened ticket.
     */
    public void proposeOpening(InteractionHook reply) {
        if ((webhook != null && to != null) || opened) {
            throw new TicketAlreadyExistsException(from);
        }

        ErrorHandler errorHandler = new ErrorHandler()
                .ignore(ErrorResponse.UNKNOWN_MESSAGE)
                .handle(ErrorResponse.CANNOT_SEND_TO_USER, exception -> handleUnableToDmUser(reply));

        // spotless:off
        MessageFactory.create()
                .addEmbeds(embeds.proposeOpening(sentEmote.getFormatted()))
                .addActionRow(actionRow -> actionRow
                        .addButton("?", button -> button
                                .setText("Raison : ")
                                .setDisabled(true)
                        )
                )
                .addActionRow(actionRow -> actionRow
                        .addSelectMenu("opening-menu", selectMenu -> selectMenu
                                .addOption(Emoji.fromUnicode("📝"), "Signalement utilisateur", "op-user-report")
                                .addOption(Emoji.fromUnicode("⛔"), "Contester une sanction", "op-unban")
                                .addOption(Emoji.fromUnicode("✨"), "Proposer une amélioration", "op-enhancement")
                                .addOption(Emoji.fromUnicode("\uD83D\uDCAC"), "Autre", "op-other")
                        )
                )
                .send(from)
                .queue(
                        msg -> reply.editOriginal("➡️ " + msg.getChannel().getAsMention()).queue(),
                        errorHandler
                );
        // spotless:on
    }

    /**
     * Directly opens a ticket without asking for the user permission.
     */
    public void forceOpening(User by) throws IOException {
        // spotless:off
        MessageFactory.create()
                .addEmbeds(embeds.forceOpening(sentEmote.getFormatted()))
                .send(from)
                .queue();
        // spotless:on

        openOnServer(true, by, new TicketOpeningReason.Empty());
    }

    public void openOnServer(boolean forced, User by, TicketOpeningReason reason) throws IOException {
        if (opened) {
            throw new TicketAlreadyExistsException(from);
        }

        Category category = moderationGuild.getCategoryById(config.getString("config.ticket_guild.tickets_category"));
        TextChannel channel = category.createTextChannel(from.getName())
                .setTopic(from.getId())
                .complete();
        this.to = channel;
        this.webhook = retrieveWebhook();

        if (!forced) {
            String description = switch (reason) {
                case TicketOpeningReason.Simple r -> "`%s`".formatted(r.reason().trim());
                case TicketOpeningReason.UserReport r -> {
                    User user = r.user(category.getJDA());

                    String reportedUser = user == null
                            ? "`%s` (Utilisateur non trouvé)".formatted(r.userId())
                            : "%s (`%s`)".formatted(user.getAsMention(), user.getAsTag());

                    yield """
                            **Signalement utilisateur**
                                                                                        
                            Utilisateur signalé : %s
                            Raison : `%s`
                            """.formatted(reportedUser, r.reportReason().trim());
                }
                case TicketOpeningReason.Empty r -> "`Aucune raison`";
            };

            // spotless:off
            MessageEmbed reasonEmbed = new EmbedBuilder()
                    .setTitle("\uD83D\uDCDD Raison de l'ouverture du ticket")
                    .setDescription(description)
                    .setColor(0x48dbfb)
                    .build();
            MessageEmbed firstMessageSelectorEmbed = new EmbedBuilder()
                    .setTitle("Sélectionnez le premier message à envoyer :")
                    .setColor(0x48dbfb)
                    .build();

            MessageFactory.create()
                    .addEmbeds(reasonEmbed)
                    .send(channel)
                    .complete();

            MessageFactory.create()
                    .addEmbeds(firstMessageSelectorEmbed)
                    .addActionRow(actionRow -> actionRow
                            .addSelectMenu("first-sentence", selectMenu -> selectMenu
                                    .addOption(Emoji.fromUnicode("☀️"), "Bonjour", "bonjour")
                                    .addOption(Emoji.fromUnicode("🌙"), "Bonsoir", "bonsoir")
                            )
                    )
                    .addActionRow(TicketActionRow::addDeleteButton)
                    .send(channel)
                    .complete();
            // spotless:on
        }

        TextChannel ticketChannel =
                moderationGuild.getTextChannelById(config.getString("config.ticket_guild.channels_ids.tickets"));

        // spotless:off
        MessageFactory.create()
                .addEmbeds(embeds.ticketOpening(forced, by, from, channel, reason.reason()))
                .addActionRow(actionRow -> actionRow
                        .addButton(button -> button
                                .setText("Aller au salon")
                                .setLink(channel.getJumpUrl())
                        )
                        .addButton(button -> button
                                .setText("Aller à l'utilisateur")
                                .setLink("https://discord.com/users/%s".formatted(from.getId()))
                        )
                )
                .send(ticketChannel)
                .queue();
        // spotless:on

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
        Executors.newSingleThreadExecutor().execute(() -> {
            WebhookMessageBuilder builder = WebhookMessageAdapter.fromJDA(message);
            webhook.send(builder.build())
                    .thenAccept(msg -> message.addReaction(sentEmote).queue())
                    .exceptionally((error) -> {
                        message.addReaction(Emoji.fromUnicode("❌")).queue();
                        return null;
                    });
        });
    }

    public void sendToUser(Message message) {
        String content = message.getContentRaw().substring(1).trim();

        EmbedBuilder confirmEmbed = new EmbedBuilder()
                .setTitle("Confirmer l'envoi du message ?")
                .setDescription(content)
                .setColor(Color.ORANGE)
                .setFooter("⚠️ Tant que l'envoi du message n'a pas été confirmé, vous pouvez éditer son contenu.")
                .addField(
                        "🔗 Identifiant du message",
                        String.format("[%s](%s)", message.getId(), message.getJumpUrl()),
                        true);

        if (message.getAttachments().size() != 0) {
            confirmEmbed.addField(
                    "📎 Pièces jointes :",
                    "`"
                            + message.getAttachments().stream()
                                    .map(Message.Attachment::getFileName)
                                    .collect(Collectors.joining("`, `"))
                            + "`",
                    true);
        }

        // spotless:off
        MessageFactory.create()
                .addEmbeds(confirmEmbed)
                .addActionRow(actionRow -> actionRow
                        .addButton("confirm-message", button -> button
                                .setStyle(ButtonStyle.SUCCESS)
                                .setText("Confirmer")
                        )
                        .addButton("deny-message", button -> button
                                .setStyle(ButtonStyle.DANGER)
                                .setText("Annuler")
                        )
                        .addDeleteButton()
                )
                .send(message.getChannel())
                .queue();
        // spotless:on
    }

    public CompletableFuture<Message> confirmSendToUser(Message message) {
        CompletableFuture<Message> cf = new CompletableFuture<>();

        Executors.newSingleThreadExecutor().execute(() -> {
            String content = message.getContentRaw().startsWith("'")
                    ? message.getContentRaw().substring(1).trim()
                    : message.getContentRaw().trim();

            // spotless:off
            MessageFactory.create()
                    .setTextContent(content)
                    .apply(builder -> {
                        FileUpload[] files = message.getAttachments().stream()
                                .map(attachment -> FileUpload.fromData(
                                        attachment.getProxy().download().join(),
                                        attachment.getFileName())
                                )
                                .toArray(FileUpload[]::new);
                        return builder.addFiles(files);
                    })
                    .send(from)
                    .queue(cf::complete, e -> embeds.errorMessage(e.getMessage())
                            .send(to)
                            .queue());
            // spotless:on
        });

        return cf;
    }

    public void close() {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<Message> messages = null;
            try {
                messages =
                        to.getIterableHistory().takeWhileAsync(Objects::nonNull).get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
            Collections.reverse(messages);

            SerializableMessageArray sma = new SerializableMessageArray(
                    config.getString("config.ticket_guild.channels_ids.attachements"), from);

            messages.forEach(sma::addMessage);

            String json = GSON.toJson(sma);
            TextChannel reportsChannel =
                    moderationGuild.getTextChannelById(config.getString("config.ticket_guild.channels_ids.reports"));
            Message report = reportsChannel
                    .sendMessage("Rapport du ticket de `@" + from.getAsTag() + "`")
                    .addFiles(FileUpload.fromData(json.getBytes(StandardCharsets.UTF_8), to.getName() + ".json"))
                    .complete();

            String reportJsonUrl = report.getAttachments().get(0).getUrl();
            TextChannel ticketsChannel =
                    moderationGuild.getTextChannelById(config.getString("config.ticket_guild.channels_ids.tickets"));

            MessageFactory.create()
                    .addEmbeds(embeds.ticketClosing(from, report.getJumpUrl()))
                    .addActionRow(actionRow -> actionRow
                            .addButton(
                                    button -> button.setText("Aller au rapport").setLink(report.getJumpUrl()))
                            .addButton(button -> button.setText("Consulter le rapport (en ligne)")
                                    .setLink("https://redstom.github.io/GravenDev-TicketReader/?input=%s"
                                            .formatted(reportJsonUrl))))
                    .send(ticketsChannel)
                    .queue();

            // spotless:off
            MessageFactory.create()
                    .addActionRow(actionRow -> actionRow
                            .addButton(button -> button
                                    .setText("Aller au rapport")
                                    .setLink(report.getJumpUrl())
                            )
                            .addButton(button -> button
                                    .setText("Consulter le rapport (en ligne)")
                                    .setLink("https://redstom.github.io/GravenDev-TicketReader/?input=%s".formatted(reportJsonUrl))
                            )
                    )
                    .send(ticketsChannel)
                    .queue();
            // spotless:on

            String ticketCommand = from.getJDA().retrieveCommands().complete().stream()
                    .filter(a -> a.getName().equalsIgnoreCase("ticket"))
                    .findFirst()
                    .map(ICommandReference::getAsMention)
                    .orElse("`/ticket`");

            EmbedBuilder closedEmbed = new EmbedBuilder()
                    .setColor(Color.RED)
                    .setTitle("Ticket fermé.")
                    .setDescription(
                            "La modération a fermé le ticket avec vous. Si vous souhaitez le rouvrir, refaites la commande %s."
                                    .formatted(ticketCommand));

            MessageFactory.create().addEmbeds(closedEmbed).send(from).queue();

            to.delete().queue(ignored -> ticketManager.remove(from));
        });
    }

    private void handleUnableToDmUser(InteractionHook reply) {
        ticketManager.remove(from);

        String errorMessage = "Impossible d'envoyer un message privé à cet utilisateur!";
        MessageEmbed embed = embeds.error(errorMessage).build();

        // spotless:off
        MessageFactory.create()
                .addEmbeds(embed)
                .editReply(reply)
                .queue();
        // spotless:on
    }
}
