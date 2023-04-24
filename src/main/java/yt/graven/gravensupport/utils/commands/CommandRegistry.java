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

    private final Map<String, ICommand> commands = new HashMap<>();

    public CommandRegistry(ApplicationContext ctx, JDA jda) {
        this.ctx = ctx;
        this.jda = jda;
    }

    public void loadAll() {
        ctx.getBeansWithAnnotation(Command.class).values().stream()
                .filter(ICommand.class::isInstance)
                .map(ICommand.class::cast)
                .forEach(this::load);

        jda.updateCommands()
                .addCommands(commands.values().stream()
                        .map(ICommand::getSlashCommandData)
                        .toList())
                .queue();
    }

    public void load(ICommand command) {
        log.info(
                "Loaded command \"{}\" into {}.",
                command.getName(),
                command.getClass().getSimpleName());
        this.commands.put(command.getName(), command);
    }

    public Optional<ICommand> getCommandByName(String name) {
        return Optional.ofNullable(commands.get(name));
    }
}
