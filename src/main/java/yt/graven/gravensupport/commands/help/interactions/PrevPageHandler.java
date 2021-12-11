package yt.graven.gravensupport.commands.help.interactions;

import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import org.springframework.beans.factory.annotation.Autowired;
import yt.graven.gravensupport.commands.help.HelpManager;
import yt.graven.gravensupport.utils.interactions.IIInteractionAction;

public class PrevPageHandler implements IIInteractionAction<ButtonClickEvent> {

    @Autowired
    private HelpManager helpManager;

    @Override
    public void run(ButtonClickEvent event) {
        event.deferEdit()
            .setEmbeds(helpManager.getPrevPage(event.getMessageIdLong()))
            .queue();
    }
}
