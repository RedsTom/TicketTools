package yt.graven.gravensupport.utils.commands;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import yt.graven.gravensupport.utils.exceptions.CommandCancelledException;
import yt.graven.gravensupport.utils.exceptions.TicketException;

import java.io.IOException;

public interface ICommand {

    String[] getNames();

    String getDescription();

    void run(MessageReceivedEvent event, String[] args) throws TicketException, IOException, CommandCancelledException;

    default boolean isShown() {
        return true;
    }

}
