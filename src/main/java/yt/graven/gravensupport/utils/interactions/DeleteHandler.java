package yt.graven.gravensupport.utils.interactions;

import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;

public class DeleteHandler implements IIInteractionAction<ButtonClickEvent> {
    @Override
    public void run(ButtonClickEvent event) {
        try {
            event.deferEdit().queue();
            event.getMessage().delete().queue();
        } catch (Exception ignored) {}
    }
}
