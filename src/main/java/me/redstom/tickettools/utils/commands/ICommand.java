package me.redstom.tickettools.utils.commands;

import java.io.IOException;
import me.redstom.tickettools.utils.exceptions.CommandCancelledException;
import me.redstom.tickettools.utils.exceptions.TicketException;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

public interface ICommand {

    String getName();

    SlashCommandData getSlashCommandData();

    void run(SlashCommandInteractionEvent event) throws TicketException, IOException, CommandCancelledException;
}
