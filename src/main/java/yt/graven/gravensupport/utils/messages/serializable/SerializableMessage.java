package yt.graven.gravensupport.utils.messages.serializable;

import com.google.gson.annotations.Expose;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class SerializableMessage {

    @Expose private SerializableMessageAuthor author = new SerializableMessageAuthor();
    @Expose private long creationTimestamp = Instant.EPOCH.toEpochMilli();
    @Expose private String content = "";
    @Expose private List<String> attachementUrls = new ArrayList<>();
    @Expose private boolean edited = false;
    @Expose private MessageType messageType;

    public SerializableMessage() {
    }

    public SerializableMessage(SerializableMessageAuthor author, long creationDate, String content, List<String> attachementUrls, boolean edited, MessageType messageType) {
        this.author = author;
        this.creationTimestamp = creationDate;
        this.content = content;
        this.attachementUrls = attachementUrls;
        this.edited = edited;
        this.messageType = messageType;
    }

    public SerializableMessageAuthor getAuthor() {
        return author;
    }

    public void setAuthor(SerializableMessageAuthor author) {
        this.author = author;
    }

    public long getCreationTimestamp() {
        return creationTimestamp;
    }

    public void setCreationTimestamp(Instant creationTimestamp) {
        this.creationTimestamp = creationTimestamp.toEpochMilli();
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public List<String> getAttachementUrls() {
        return attachementUrls;
    }

    public void setAttachementUrls(List<String> attachementUrls) {
        this.attachementUrls = attachementUrls;
    }

    public boolean isEdited() {
        return edited;
    }

    public void setEdited(boolean edited) {
        this.edited = edited;
    }

    public void setCreationTimestamp(long creationTimestamp) {
        this.creationTimestamp = creationTimestamp;
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public void setMessageType(MessageType messageType) {
        this.messageType = messageType;
    }
}
