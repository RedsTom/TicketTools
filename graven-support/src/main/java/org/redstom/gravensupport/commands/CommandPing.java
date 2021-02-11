package org.redstom.gravensupport.commands;

import org.javacord.api.event.message.MessageCreateEvent;
import org.redstom.botapi.server.IServer;
import org.redstom.gravensupport.utils.command.Command;
import org.redstom.gravensupport.utils.command.CommandExecutor;

public class CommandPing implements CommandExecutor {
    @Override
    public void run(IServer server, MessageCreateEvent event, Command command, String[] args) {
        event.getChannel().sendMessage("Pong !");
    }
}
