package yt.graven.gravensupport.commands.ticket;

import club.minnced.discord.webhook.WebhookClientBuilder;
import club.minnced.discord.webhook.external.JDAWebhookClient;
import club.minnced.discord.webhook.send.WebhookMessageBuilder;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.interactions.components.Button;
import net.dv8tion.jda.api.interactions.components.ButtonStyle;
import net.dv8tion.jda.api.utils.AttachmentOption;
import org.simpleyaml.configuration.file.YamlConfiguration;
import yt.graven.gravensupport.utils.exceptions.TicketAlreadyExistsException;
import yt.graven.gravensupport.utils.messages.Embeds;
import yt.graven.gravensupport.utils.messages.TMessage;
import yt.graven.gravensupport.utils.messages.serializable.SerializableMessageArray;

import java.awt.*;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class Ticket {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().excludeFieldsWithoutExposeAnnotation().create();

    public static Ticket loadFromChannel(TicketManager ticketManager, Embeds embeds, YamlConfiguration config, TextChannel channel) throws IOException {
        String topic = channel.getTopic();
        topic = topic == null ? "0" : topic;

        User user = channel.getJDA().retrieveUserById(topic).complete();
        if (user == null) {
            throw new RuntimeException("Error : Unable to find an user matching the ticket #" + channel.getName() + " !");
        }

        Ticket t = new Ticket(ticketManager, embeds, config, user, channel, null);
        t.webhookTransmitter = t.retrieveWebhook();

        return t;
    }

    private final TicketManager ticketManager;

    private final Embeds embeds;
    private final YamlConfiguration config;

    private final User from;
    private final Guild moderationGuild;
    private final Emote sentEmote;
    private TextChannel to;
    private JDAWebhookClient webhookTransmitter;
    private boolean opened;

    /**
     * Creates a new ticket
     *
     * @param from User to create the ticket with
     */
    public Ticket(TicketManager ticketManager, Embeds embeds, YamlConfiguration config, User from) {
        this(ticketManager, embeds, config, from, null, null);
        this.opened = false;
    }

    private Ticket(TicketManager ticketManager, Embeds embeds, YamlConfiguration config, User from, TextChannel to, JDAWebhookClient webhookTransmitter) {
        this.embeds = embeds;
        this.from = from;
        this.to = to;
        this.webhookTransmitter = webhookTransmitter;
        this.config = config;
        this.opened = true;
        this.ticketManager = ticketManager;

        this.moderationGuild = from.getJDA().getGuildById(config.getString("config.ticket_guild.guild_id"));
        this.sentEmote = this.moderationGuild.getEmoteById(this.config.getString("config.ticket_guild.reaction_id"));
    }

    /**
     * Sends a message to confirm the opening of the ticket in the case of a user-opened ticket.
     */
    public void proposeOpening() {
        if ((webhookTransmitter != null && to != null) || opened) {
            throw new TicketAlreadyExistsException(from);
        }

        from.openPrivateChannel()
            .complete()
            .sendMessage(
                TMessage.from(embeds.proposeOpening(sentEmote.getAsMention()))
                    .actionRow()
                    .add(Button.of(ButtonStyle.SUCCESS, "validate-opening", "Ouvrir le ticket", Emoji.fromUnicode("✅")))
                    .build()
                    .build()
            )
            .queue();
    }

    /**
     * Directly opens a ticket without asking for the user permission.
     */
    public void forceOpening(User by) throws IOException {
        TMessage
            .from(embeds.forceOpening(sentEmote.getAsMention()))
            .sendMessage(from)
            .queue();

        openOnServer(true, by);
    }

    public void openOnServer(boolean forced, User by) throws IOException {
        if (opened) {
            throw new TicketAlreadyExistsException(from);
        }

        Category category = moderationGuild.getCategoryById(config.getString("config.ticket_guild.tickets_category"));
        TextChannel channel = category
            .createTextChannel(from.getName())
            .setTopic(from.getId())
            .complete();
        this.to = channel;
        this.webhookTransmitter = retrieveWebhook();

        if (!forced) {
            TMessage.create()
                .setContent("Sélectionnez le premier message à envoyer :")
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
                .queue();
        }

        TextChannel ticketChannel =
            moderationGuild.getTextChannelById(config.getString("config.ticket_guild.channels_ids.tickets"));
        TMessage.from(embeds.ticketOpening(forced, by, from, channel))
            .actionRow()
            .button()
            .withText("Aller au salon")
            .withLink(String.format("https://discord.com/channels/%s/%s", moderationGuild.getId(), channel.getId()))
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

    private JDAWebhookClient retrieveWebhook() throws IOException {
        List<Webhook> webhooks = to.retrieveWebhooks().complete();
        Webhook webhook = switch (webhooks.size()) {
            case 0 -> to
                .createWebhook(from.getName())
                .setAvatar(Icon.from(
                    new URL(from.getEffectiveAvatarUrl()).openStream()
                ))
                .complete();
            default -> webhooks.get(0);
        };
        JDAWebhookClient client = new WebhookClientBuilder(webhook.getIdLong(), webhook.getToken())
            .buildJDA();

        return client;
    }

    public TextChannel getTo() {
        return to;
    }

    public User getFrom() {
        return from;
    }

    public JDAWebhookClient getWebhookTransmitter() {
        return webhookTransmitter;
    }

    public boolean isOpened() {
        return opened;
    }

    public void sendToTicket(Message message) {
        Executors.newSingleThreadExecutor().execute(() -> {
            WebhookMessageBuilder webhookMessageBuilder = WebhookMessageBuilder.fromJDA(message);
            for (Message.Attachment attachement : message.getAttachments()) {
                webhookMessageBuilder.addFile(attachement.downloadToFile().join());
            }
            this.getWebhookTransmitter().send(webhookMessageBuilder.build());
            message.addReaction(sentEmote).queue();
        });
    }

    public void sendToUser(Message message) {
        String content = message.getContentRaw().substring(1).trim();

        EmbedBuilder embedBuilder = new EmbedBuilder()
            .setTitle("Confirmer l'envoi du message ?")
            .setDescription(content)
            .setColor(Color.ORANGE)
            .setFooter("⚠️ Tant que l'envoi du message n'a pas été confirmé, vous pouvez éditer son contenu.")
            .addField(
                "🔗 Identifiant du message",
                String.format("[%s](%s)", "" + message.getId(), "" + message.getJumpUrl()),
                true
            );

        if (message.getAttachments().size() != 0) {
            embedBuilder.addField("📎 Pièces jointes :",
                "`" + message.getAttachments()
                    .stream()
                    .map(Message.Attachment::getFileName)
                    .collect(Collectors.joining("`, `")) + "`",
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

    public CompletableFuture<Message> confirmSendToUser(Message message) {
        CompletableFuture<Message> cf = new CompletableFuture<>();

        Executors.newSingleThreadExecutor().execute(() -> {
            TMessage builder = TMessage.from(new MessageBuilder(message))
                .setContent(message.getContentRaw().startsWith("'")
                    ? message.getContentRaw().substring(1).trim()
                    : message.getContentRaw().trim());

            try {
                builder.setFiles(message.getAttachments().stream()
                    .map(a -> a.downloadToFile().join())
                    .collect(Collectors.toList()));
            } catch (UnsupportedOperationException ignored) {}

            builder
                .sendMessage(getFrom())
                .queue(cf::complete);
        });

        return cf;
    }

    public void close() {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<Message> messages = to.getIterableHistory().complete();
            Collections.reverse(messages);

            SerializableMessageArray sma = new SerializableMessageArray(
                config.getString("config.ticket_guild.channels_ids.attachements"), from);

            messages.forEach(sma::addMessage);

            String json = GSON.toJson(sma);
            TextChannel reportsChannel = moderationGuild.getTextChannelById(
                config.getString("config.ticket_guild.channels_ids.reports")
            );
            Message report = reportsChannel
                .sendMessage("Rapport du ticket de `@" + from.getAsTag() + "`")
                .addFile(json.getBytes(StandardCharsets.UTF_8), to.getName() + ".json")
                .complete();

            TextChannel ticketsChannel = moderationGuild.getTextChannelById(
                config.getString("config.ticket_guild.channels_ids.tickets")
            );
            TMessage.from(embeds.ticketClosing(from, report.getJumpUrl()))
                .actionRow()
                .button()
                .withText("Aller au rapport")
                .withLink(report.getJumpUrl())
                .build()
                .build()
                .sendMessage(ticketsChannel)
                .complete();


            TMessage.create()
                .setEmbeds(new EmbedBuilder()
                    .setColor(Color.RED)
                    .setTitle("Ticket fermé.")
                    .setDescription("La modération a fermé le ticket avec vous. Si vous souhaitez le rouvrir, refaites la commande `" + config.getString("config.prefix") + "new`"))
                .sendMessage(from)
                .queue();

            to.delete().queue((v1) -> ticketManager.remove(from));
        });
    }
}