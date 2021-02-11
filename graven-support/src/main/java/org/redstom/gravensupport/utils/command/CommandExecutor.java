package org.redstom.gravensupport.utils.command;

import org.javacord.api.event.message.MessageCreateEvent;
import org.redstom.botapi.server.IServer;

public interface CommandExecutor {

    void run(IServer server, MessageCreateEvent event, Command command, String[] args);

}
