package yt.graven.gravensupport.utils.messages.builder.data;

import lombok.Setter;
import lombok.experimental.Accessors;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;

@Accessors(chain = true)
public class TicketButton {

    @Setter
    private String id;

    @Setter
    private ButtonStyle style;

    @Setter
    private String text;

    @Setter
    private Emoji emoji;

    @Setter
    private String link;

    public TicketButton() {}

    public TicketButton(String id) {
        this.id = id;
    }

    public Button build() {
        if (style == null) {
            style = ButtonStyle.SECONDARY;
        }

        if(link != null) {
            style = ButtonStyle.LINK;
        }

        if (id == null && link == null) {
            throw new IllegalStateException("Button must have an id or link");
        }
        if (id != null && link != null) {
            throw new IllegalStateException("Button cannot have both an id and link");
        }

        String idOrLink = id != null ? id : link;
        return Button.of(style, idOrLink, text, emoji);
    }
}
