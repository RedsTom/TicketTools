package yt.graven.gravensupport.utils.messages.serializable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;

public class SerializableMessageArray {

  @Expose private final int version = 1;

  @Expose(deserialize = false, serialize = false)
  public User fromUser;

  @Expose(deserialize = false, serialize = false)
  private String attachementsChannelId;

  @Expose
  @SerializedName("to")
  private SerializableMessageAuthor from;

  @Expose private List<SerializableMessage> messages = new ArrayList<>();

  public SerializableMessageArray(String attachementsChannelId, User from) {
    this.attachementsChannelId = attachementsChannelId;
    this.fromUser = from;

    this.from = new SerializableMessageAuthor();
    this.from.setId(from.getIdLong());
    this.from.setName(from.getAsTag());
    this.from.setAvatarUrl(from.getAvatarUrl());
  }

  public void addMessage(Message message) {
    SerializableMessage sMessage = new SerializableMessage();
    sMessage.setMessageType(MessageType.MODERATION);

    SerializableMessageAuthor author = new SerializableMessageAuthor();
    if (message.getAuthor().getDiscriminator().equalsIgnoreCase("0000")) {
      author.setAvatarUrl(fromUser.getAvatarUrl());
      author.setName(fromUser.getAsTag());
      author.setId(fromUser.getIdLong());

      sMessage.setMessageType(MessageType.TARGET);
    } else {
      author.setAvatarUrl(message.getAuthor().getAvatarUrl());
      author.setName(message.getAuthor().getAsTag());
      author.setId(message.getAuthor().getIdLong());
    }

    sMessage.setAuthor(author);
    sMessage.setContent(message.getContentRaw());
    sMessage.setEdited(message.isEdited());
    sMessage.setCreationTimestamp(message.getTimeCreated().toInstant());

    if (message.getAttachments().size() != 0) {
      TextChannel channel = fromUser.getJDA().getTextChannelById(attachementsChannelId);
      message
          .getAttachments()
          .forEach(
              attachment -> {
                File f = attachment.downloadToFile().join();
                Message msg =
                    channel
                        .sendMessage("Attachement of @" + message.getAuthor().getAsTag())
                        .addFile(f)
                        .complete();

                for (Message.Attachment msgAttachment : msg.getAttachments()) {
                  sMessage.getAttachementUrls().add(msgAttachment.getUrl());
                }
                f.delete();
              });
    }

    if (message.getEmbeds().size() != 0) {
      for (MessageEmbed embed : message.getEmbeds()) {
        sMessage.addEmbed(embed);
      }
    }

    messages.add(sMessage);
  }
}
