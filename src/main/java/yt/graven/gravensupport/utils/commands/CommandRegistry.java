package yt.graven.gravensupport.utils.commands;

import java.util.*;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CommandRegistry {

  private final ApplicationContext ctx;
  private final JDA jda;

  private final List<ICommand> commands = new ArrayList<>();

  public CommandRegistry(ApplicationContext ctx, JDA jda) {
    this.ctx = ctx;
    this.jda = jda;
  }

  public void loadAll() {
    ctx.getBeansWithAnnotation(Command.class).values().stream()
        .filter(ICommand.class::isInstance)
        .map(ICommand.class::cast)
        .peek(
            a ->
                log.info(
                    "Loaded command \"{}\" into {}.", a.getName(), a.getClass().getSimpleName()))
        .forEach(this::loadOne);

    jda.updateCommands()
        .addCommands(commands.stream().map(ICommand::getSlashCommandData).toList())
        .queue();
  }

  public void loadOne(ICommand command) {
    this.commands.add(command);
  }

  public Optional<ICommand> getCommandByName(String name) {
    return commands.stream()
        .peek(a -> log.info("{} ; {}", a.getName(), name))
        .filter(a -> name.startsWith(a.getName()))
        .findFirst();
  }

  public List<ICommand> getCommands() {
    return commands;
  }
}
