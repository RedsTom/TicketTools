package org.redstom.gravensupport.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.ServerChannel;
import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.entity.webhook.IncomingWebhook;
import org.javacord.api.entity.webhook.Webhook;
import org.redstom.botapi.server.IServer;
import org.redstom.gravensupport.listener.MessageListener;

import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class Ticket {

    private final User from;
    private final ServerTextChannel to;
    private IncomingWebhook webhook;

    Ticket(User from, ServerTextChannel to) {
        this.from = from;
        this.to = to;
    }

    public void sendOpeningAnnounce(IServer iServer, boolean dm) {
        Server server = to.getServer();
        ServerChannel channel = server.getChannelById(MessageListener.TICKETS_CHANNEL_ID).get();
        EmbedBuilder embed = new EmbedBuilder()
            .setTitle("Ticket ouvert")
            .setDescription("Un nouveau ticket a été ouvert")
            .addField(":information_source: **Détails**",
                "> **Identifiant de l'utilisateur**\n" +
                    "" + from.getId() + "\n\n" +
                    "> **Nom de l'utilisateur**\n" +
                    "" + from.getMentionTag() + "\n\n" +
                    ":hash: **Salon**\n" +
                    to.getMentionTag())
            .setThumbnail(from.getAvatar())
            .setColor(Color.GREEN);
        channel.asServerTextChannel().get().sendMessage(embed);
        if (dm) sendOpeningDM(iServer);
    }

    void sendOpeningDM(IServer iServer) {
        from.sendMessage(new EmbedBuilder()
            .setAuthor("Message automatique")
            .setTitle("Ticket ouvert !")
            .setDescription("Votre ticket a bien été ouvert, vous pouvez maintenant discuter avec la " +
                "modération en envoyant un message ici")
            .addField(":notebook_with_decorative_cover: Notes à propos de vos messages : ",
                ":warning: Les messages que vous envoyez " +
                    "ne peuvent ni être édités, ni être supprimés. Faites donc bien attention au contenu que vous " +
                    "envoyez. \n" +
                    ":warning: Les règles du discord `Graven - Développement` s'appliquent également dans cette " +
                    "conversation")
            .addField(":notebook_with_decorative_cover: Note :",
                "La réaction <:sent:" + MessageListener.REACTION_ID + "> " +
                    "sera ajoutée à vos message si " +
                    "le message est bien transmis. \n" +
                    "Dans le cas où elle n'apparaitrai pas dans la minute suivant l'envoi de votre message, " +
                    "faites la commande **`" + iServer.getPrefix() + "new`** sur le serveur et renvoyez votre message")
            .setColor(Color.GREEN));
    }

    public void sendClosingAnnounce(String reportUrl) {
        Server server = to.getServer();
        ServerChannel channel = server.getChannelById(MessageListener.TICKETS_CHANNEL_ID).get();
        EmbedBuilder embed = new EmbedBuilder()
            .setTitle("Ticket fermé !")
            .setDescription("Le ticket a été fermé")
            .addField(":information_source: **Détails**",
                "> Identifiant de l'utilisateur\n" +
                    "" + from.getId() + "\n\n" +
                    "> Nom de l'utilisateur\n" +
                    "" + from.getMentionTag() + "\n\n" +
                    ":spiral_note_pad: **Rapport**\n" +
                    "[Lien du rapport](" + reportUrl + ")")
            .setThumbnail(from.getAvatar())
            .setColor(Color.RED);
        channel.asServerTextChannel().get().sendMessage(embed);
        from.sendMessage(new EmbedBuilder()
            .setColor(Color.RED)
            .setTitle("Ticket fermé !")
            .setDescription("Votre ticket a été fermé par un modérateur, si vous souhaitez le ré-ouvrir, renvoyez" +
                " un message dans ce salon."));
    }

    public void close() {
        this.generateReport()
            .thenAccept(url -> to.asServerChannel().get()
                .delete("Ticket closed")
                .thenAccept(n1 -> {
                    sendClosingAnnounce(url);
                    TicketManager.removeTicket(this);
                }));
    }

    public CompletableFuture<String> generateReport() {
        to.sendMessage(":warning: Le rapport est en cours de génération," +
            " cela peut prendre un peu de temps. Veuillez patienter");
        CompletableFuture<String> future = new CompletableFuture<>();
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        List<SerializableMessage> array = new ArrayList<>();
        to.getMessagesAsStream()
            .forEach(message -> {
                array.add(SerializableMessage.fromMessage(message));
            });
        InputStream stream = new ByteArrayInputStream(gson.toJson(array).getBytes());
        to.getServer()
            .getTextChannelById(MessageListener.REPORTS_CHANNEL_ID).get()
            .sendMessage(stream, to.getName() + ".json")
            .thenAccept(msg -> {
                future.complete(msg.getLink().toString());
            });
        return future;
    }

    public void sendToUser(MessageBuilder builder) {
        builder.send(from);
    }

    public void sendToTicket(IServer iServer, DiscordApi api, MessageBuilder builder) {
        if (api.getServerById(MessageListener.SUPPORT_SERVER_ID).get().getTextChannelById(to.getId()).isEmpty()) {
            System.out.println("Cannot find any channel");
            TicketManager.removeTicket(this);
            TicketManager.createTicket(iServer, from, false).thenAccept(ticket -> {
                ticket.sendToTicket(iServer, api, builder);
            });
            return;
        }
        updateWebhook().thenAccept((webhook) -> builder.sendWithWebhook(api,
            this.webhook.getId(),
            this.webhook.getToken()));
    }

    public CompletableFuture<Webhook> updateWebhook() {
        return webhook.createUpdater()
            .setAvatar(from.getAvatar())
            .setName(from.getName())
            .update();
    }

    public CompletableFuture<Void> setupTicket() {
        CompletableFuture<Void> future = new CompletableFuture<>();
        to.updateTopic("" + from.getId());
        this.to.createWebhookBuilder()
            .setAvatar(from.getAvatar())
            .setName(from.getName())
            .create()
            .thenAccept(wb -> {
                this.setWebhook(wb);
                future.complete(null);
            });
        return future;
    }

    public IncomingWebhook getWebhook() {
        return webhook;
    }

    void setWebhook(IncomingWebhook incomingWebhook) {
        this.webhook = incomingWebhook;
    }

    public ServerTextChannel getTo() {
        return to;
    }

    public User getFrom() {
        return from;
    }

    @Override
    public String toString() {
        return "Ticket{" +
            "from=" + from +
            ", to=" + to +
            ", webhook=" + webhook +
            '}';
    }
}
