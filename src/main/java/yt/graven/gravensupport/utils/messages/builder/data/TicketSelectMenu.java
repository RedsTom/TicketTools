package yt.graven.gravensupport.utils.messages.builder.data;

import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;

public class TicketSelectMenu {

    private final StringSelectMenu.Builder builder;

    public TicketSelectMenu(String id) {
        this.builder = StringSelectMenu.create(id);
    }

    public TicketSelectMenu(StringSelectMenu.Builder builder) {
        this.builder = builder;
    }

    public TicketSelectMenu addOption(Emoji emoji, String label, String value) {
        builder.addOption(label, value, emoji);
        return this;
    }

    public StringSelectMenu build() {
        return builder.build();
    }
}
