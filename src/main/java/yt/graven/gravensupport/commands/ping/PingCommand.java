package yt.graven.gravensupport.commands.ping;

import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.entities.Emoji;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import org.springframework.stereotype.Component;
import yt.graven.gravensupport.utils.commands.ICommand;
import yt.graven.gravensupport.utils.messages.Embeds;
import yt.graven.gravensupport.utils.messages.TMessage;

@Component
@RequiredArgsConstructor
public class PingCommand implements ICommand {
  private final PingManager pingManager;

  private final Embeds embeds;

  @Override
  public String[] getNames() {
    return new String[] {"ping", "pong"};
  }

  @Override
  public String getDescription() {
    return "Sert à calculer la latence du bot";
  }

  @Override
  public void run(MessageReceivedEvent event, String[] args) {
    MessageEmbed embed = pingManager.compute();

    TMessage.from(embed)
        .actionRow()
        .add(Button.of(ButtonStyle.PRIMARY, "refresh-ping", "Actualiser", Emoji.fromUnicode("🔁")))
        .deletable()
        .build()
        .sendMessage(event.getChannel())
        .queue();
  }
}
