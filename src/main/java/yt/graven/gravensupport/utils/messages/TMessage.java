package yt.graven.gravensupport.utils.messages;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Component;
import net.dv8tion.jda.api.interactions.components.ItemComponent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu;
import net.dv8tion.jda.api.requests.restaction.MessageAction;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class TMessage {

    private final List<ActionRow> actionRows = new ArrayList<>();
    private List<File> files = new ArrayList<>();
    private MessageBuilder msgBuilder = new MessageBuilder();

    public static TMessage create() {
        return new TMessage();
    }

    public static TMessage from(MessageBuilder msgBuilder) {
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

    private TMessage setMsgBuilder(MessageBuilder msgBuilder) {
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

    public TMessage addFile(File file) {
        this.files.add(file);
        return this;
    }

    public TMessage setFiles(List<File> files) {
        this.files = files;
        return this;
    }

    public TActionRow actionRow() {
        return new TActionRow(this);
    }

    public Message build() {
        msgBuilder.setActionRows(actionRows);
        return msgBuilder.build();
    }

    public MessageAction sendMessage(MessageChannel channel) {
        MessageAction action = channel.sendMessage(this.build());
        for (File file : files) {
            action = action.addFile(file);
            file.delete();
        }
        files = new ArrayList<>();
        return action;
    }

    public MessageAction sendMessage(User user) {
        MessageAction action = user.openPrivateChannel()
            .complete()
            .sendMessage(this.build());
        for (File file : files) {
            action = action.addFile(file);
            file.delete();
        }
        files = new ArrayList<>();
        return action;
    }

    public static class TActionRow {

        private final TMessage msg;
        private final List<ItemComponent> components;

        private TActionRow(TMessage msg) {
            this.msg = msg;
            this.components = new ArrayList<>();
        }

        public TActionRow deletable() {
            return add(Button.secondary(
                "delete",
                Emoji.fromUnicode("🗑️")
            ));
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
            msg.actionRows.add(ActionRow.of(components));
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
                        tActionRow.components.add(
                            Button.link(link, text)
                        );
                    } else if (emoji != null) {
                        tActionRow.components.add(
                            Button.link(link, emoji)
                        );
                    }
                    return tActionRow;
                }
                if (text == null && emoji != null) {
                    tActionRow.components.add(
                        Button.of(buttonStyle, id, emoji)
                    );
                    return tActionRow;
                }
                if (text != null && emoji == null) {
                    tActionRow.components.add(
                        Button.of(buttonStyle, id, text)
                    );
                    return tActionRow;
                }
                tActionRow.components.add(
                    Button.of(buttonStyle, id, text, emoji)
                );
                return tActionRow;
            }
        }

        public static class TSelectMenu {
            private final TActionRow row;
            private SelectMenu.Builder builder;

            private TSelectMenu(TActionRow row, @NotNull String customId) {
                this.row = row;
                this.builder = SelectMenu.create(customId);
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
