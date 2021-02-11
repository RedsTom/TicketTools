package org.redstom.gravensupport.utils.command;

public class Command {

    private String id, description;
    private String[] aliases;
    private CommandExecutor executor;

    public Command(String id, String description, CommandExecutor executor, String... aliases) {
        this.id = id;
        this.description = description;
        this.aliases = aliases;
        this.executor = executor;
    }

    public String getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public String[] getAliases() {
        return aliases;
    }

    public CommandExecutor getExecutor() {
        return executor;
    }
}
