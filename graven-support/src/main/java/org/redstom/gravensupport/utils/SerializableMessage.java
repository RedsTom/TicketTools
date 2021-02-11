package org.redstom.gravensupport.utils;

import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageAttachment;
import org.javacord.api.entity.message.MessageBuilder;
import org.redstom.gravensupport.listener.MessageListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

public class SerializableMessage {

    private final long id;
    private final SerializableAuthor serializableAuthor;
    private final List<String> attachements;
    private final String content;

    SerializableMessage(long id, SerializableAuthor serializableAuthor, List<String> attachements, String content) {
        this.id = id;
        this.serializableAuthor = serializableAuthor;
        this.attachements = attachements;
        this.content = content;
    }

    public static SerializableMessage fromMessage(Message message) {
        CompletableFuture<List<String>> future = new CompletableFuture<>();
        message.getApi().getUserById(message.getServerTextChannel().get().getTopic()).thenAccept(user -> {
            copyAttachementsAndGetLinks(
                message.getAuthor().isWebhook() ?
                    user.getId() :
                    message.getAuthor().getId(),
                message.getApi(), message.getAttachments()
            ).thenAccept(links -> {
                future.complete(links);
            });
        });
        List<String> links = future.join();
        if (!message.getAuthor().isWebhook()) {
            return new SerializableMessage(
                message.getId(),
                new SerializableAuthor(message.getAuthor().getDiscriminatedName(),
                    Long.parseLong(message.getServerTextChannel().get().getTopic())),
                links,
                message.getContent()
            );
        } else {
            return new SerializableMessage(
                message.getId(),
                new SerializableAuthor(message.getAuthor().getDiscriminatedName(), message.getAuthor().getId()),
                links,
                message.getContent()
            );
        }
    }

    private static CompletableFuture<List<String>> copyAttachementsAndGetLinks(
        long authorId,
        DiscordApi api,
        List<MessageAttachment> attachments
    ) {
        CompletableFuture<List<String>> future = new CompletableFuture<>();
        ServerTextChannel channel = api.getServerById(MessageListener.SUPPORT_SERVER_ID).get()
            .getTextChannelById(MessageListener.ATTACHEMENTS_CHANNEL_ID).get();
        List<String> links = new ArrayList<>();
        MessageBuilder message = new MessageBuilder().setContent("Attachments of <@" + authorId + ">");
        AtomicBoolean hasAttachment = new AtomicBoolean(false);
        attachments.forEach(attachment -> {
            try {
                message.addAttachment(attachment.downloadAsInputStream(), attachment.getFileName());
                hasAttachment.set(true);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        if (hasAttachment.get()) {
            message.send(channel).thenAccept(msg -> {
                links.add(msg.getLink().toString());
                future.complete(links);
            });
        } else {
            future.complete(links);
        }
        return future;
    }

    public long getId() {
        return id;
    }

    public SerializableAuthor getSerializableAuthor() {
        return serializableAuthor;
    }

    public List<String> getAttachements() {
        return attachements;
    }

    public static class SerializableAuthor {

        private final String discriminatedName;
        private final long id;

        private SerializableAuthor(String discriminatedName, long id) {
            this.discriminatedName = discriminatedName;
            this.id = id;
        }

        public String getDiscriminatedName() {
            return discriminatedName;
        }

        public long getId() {
            return id;
        }
    }

}
