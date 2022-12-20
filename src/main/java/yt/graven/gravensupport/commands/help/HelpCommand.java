package yt.graven.gravensupport.commands.help;

import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.entities.Emoji;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import org.springframework.stereotype.Component;
import yt.graven.gravensupport.utils.commands.ICommand;
import yt.graven.gravensupport.utils.messages.TMessage;

@Component
@RequiredArgsConstructor
public class HelpCommand implements ICommand {

  private final HelpManager helpManager;

  @Override
  public String[] getNames() {
    return new String[] {"help", "?"};
  }

  @Override
  public String getDescription() {
    return "Sert à donner l'aide du bot afin de connaître ses différentes commandes";
  }

  @Override
  public void run(MessageReceivedEvent event, String[] args) {
    helpManager.updateEmbeds();
    TMessage.from(helpManager.getCurrentPage())
        .actionRow()
        .button("prev-page")
        .withStyle(ButtonStyle.PRIMARY)
        .withText("Précédent")
        .withEmote(Emoji.fromUnicode("◀️"))
        .build()
        .button("next-page")
        .withStyle(ButtonStyle.PRIMARY)
        .withText("Suivant")
        .withEmote(Emoji.fromUnicode("▶️"))
        .build()
        .deletable()
        .build()
        .sendMessage(event.getChannel())
        .queue();
  }
}
