package yt.graven.gravensupport.utils.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class CommandRegistry {

  private final List<ICommand> commands = new ArrayList<>();

  public CommandRegistry addCommand(ICommand command) {
    if (getCommandByName(command.getNames()[0]).isPresent()) return this;
    this.commands.add(command);
    return this;
  }

  public Optional<ICommand> getCommandByName(String name) {
    return commands.stream().filter(a -> Arrays.asList(a.getNames()).contains(name)).findFirst();
  }

  public void removeCommand(String name) {
    commands.removeIf(a -> Arrays.asList(a.getNames()).contains(name));
  }

  public List<ICommand> getShownCommands() {
    return commands.stream().filter(ICommand::isShown).collect(Collectors.toList());
  }

  public List<ICommand> getCommands() {
    return commands;
  }
}
