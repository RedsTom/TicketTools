package yt.graven.gravensupport.utils.commands;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import yt.graven.gravensupport.utils.exceptions.CommandCancelledException;
import yt.graven.gravensupport.utils.exceptions.TicketException;

import java.io.IOException;

public interface ICommand {

    String getName();
    SlashCommandData getSlashCommandData();

    void run(SlashCommandInteractionEvent event) throws TicketException, IOException, CommandCancelledException;

}
