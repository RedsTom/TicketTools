package yt.graven.gravensupport.utils.messages;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.ItemComponent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;
import net.dv8tion.jda.api.requests.restaction.WebhookMessageEditAction;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import net.dv8tion.jda.api.utils.messages.MessageEditData;
import org.jetbrains.annotations.NotNull;

public class TMessage {

    private final List<ActionRow> actionRows = new ArrayList<>();
    private MessageCreateBuilder msgBuilder = new MessageCreateBuilder();

    public static TMessage create() {
        return new TMessage();
    }

    public static TMessage from(MessageCreateBuilder msgBuilder) {
        return new TMessage().setMsgBuilder(msgBuilder);
    }

    public static TMessage from(MessageEmbed... embeds) {
        TMessage msg = new TMessage();
        msg.setEmbeds(embeds);
        return msg;
    }

    public static TMessage from(EmbedBuilder... embeds) {
        TMessage msg = new TMessage();
        msg.setEmbeds(embeds);
        return msg;
    }

    private TMessage setMsgBuilder(MessageCreateBuilder msgBuilder) {
        this.msgBuilder = msgBuilder;
        return this;
    }

    public TMessage setEmbeds(MessageEmbed... embeds) {
        msgBuilder.setEmbeds(embeds);
        return this;
    }

    public TMessage setEmbeds(EmbedBuilder... embeds) {
        msgBuilder.setEmbeds(Arrays.stream(embeds).map(EmbedBuilder::build).collect(Collectors.toList()));
        return this;
    }

    public TMessage setContent(String content) {
        if (content.isEmpty()) content = "** **";
        msgBuilder.setContent(content);
        return this;
    }

    public TMessage addFiles(FileUpload... files) {
        msgBuilder.addFiles(files);
        return this;
    }

    public TMessage setFiles(List<FileUpload> files) {
        msgBuilder.setFiles(files);
        return this;
    }

    public TActionRow actionRow() {
        return new TActionRow(this);
    }

    public MessageEditData buildEdit() {
        return MessageEditData.fromCreateData(build());
    }

    public MessageCreateData build() {
        msgBuilder.setComponents(actionRows);
        return msgBuilder.build();
    }

    public MessageCreateAction sendMessage(MessageChannel channel) {
        MessageCreateAction action = channel.sendMessage(this.build());
        return action;
    }

    public MessageCreateAction sendMessage(User user) {
        MessageCreateAction action = user.openPrivateChannel().complete().sendMessage(this.build());
        return action;
    }

    public ReplyCallbackAction reply(SlashCommandInteractionEvent event) {
        return event.deferReply(true).applyData(build());
    }

    public ReplyCallbackAction reply(SlashCommandInteractionEvent event, boolean ephemeral) {
        return event.deferReply(ephemeral).applyData(build());
    }

    public WebhookMessageEditAction<Message> editReply(InteractionHook reply) {
        return reply.editOriginal(buildEdit());
    }

    public static class TActionRow {

        private final TMessage msg;
        private final List<ItemComponent> components;

        private TActionRow(TMessage msg) {
            this.msg = msg;
            this.components = new ArrayList<>();
        }

        public TActionRow deletable() {
            return add(Button.secondary("delete", Emoji.fromUnicode("🗑️")));
        }

        public TButton button(String id) {
            return new TButton(this, id);
        }

        public TButton button() {
            return new TButton(this);
        }

        public TActionRow add(ItemComponent component) {
            this.components.add(component);
            return this;
        }

        public TMessage build() {
            if (!components.isEmpty()) {
                msg.actionRows.add(ActionRow.of(components));
            }
            return msg;
        }

        public TSelectMenu selectMenu(String id) {
            return new TSelectMenu(this, id);
        }

        public static class TButton {

            private final TActionRow tActionRow;
            private ButtonStyle buttonStyle;
            private final String id;
            private String text;
            private Emoji emoji;
            private String link;

            public TButton(TActionRow tActionRow, String id) {
                this.tActionRow = tActionRow;
                this.id = id;
            }

            public TButton(TActionRow tActionRow) {
                this(tActionRow, null);
            }

            public TButton withStyle(ButtonStyle style) {
                this.buttonStyle = style;
                return this;
            }

            public TButton withText(String text) {
                this.text = text;
                return this;
            }

            public TButton withEmote(Emoji emoji) {
                this.emoji = emoji;
                return this;
            }

            public TButton withLink(String link) {
                this.link = link;
                return this;
            }

            public TActionRow build() {
                if (buttonStyle == null) {
                    buttonStyle = ButtonStyle.SECONDARY;
                }
                if (link != null) {
                    if (text != null) {
                        tActionRow.components.add(Button.link(link, text));
                    } else if (emoji != null) {
                        tActionRow.components.add(Button.link(link, emoji));
                    }
                    return tActionRow;
                }
                if (text == null && emoji != null) {
                    tActionRow.components.add(Button.of(buttonStyle, id, emoji));
                    return tActionRow;
                }
                if (text != null && emoji == null) {
                    tActionRow.components.add(Button.of(buttonStyle, id, text));
                    return tActionRow;
                }
                tActionRow.components.add(Button.of(buttonStyle, id, text, emoji));
                return tActionRow;
            }
        }

        public static class TSelectMenu {
            private final TActionRow row;
            private StringSelectMenu.Builder builder;

            private TSelectMenu(TActionRow row, @NotNull String customId) {
                this.row = row;
                this.builder = StringSelectMenu.create(customId);
            }

            public TSelectMenu addOption(String label, String value, Emoji emoji) {
                this.builder.addOption(label, value, emoji);
                return this;
            }

            public TActionRow build() {
                row.components.add(builder.build());
                return row;
            }
        }
    }
}
