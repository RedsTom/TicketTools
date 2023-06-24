package me.redstom.tickettools.commands.ticket.create;

import java.io.IOException;
import lombok.RequiredArgsConstructor;
import me.redstom.tickettools.commands.ticket.TicketManager;
import me.redstom.tickettools.utils.commands.Command;
import me.redstom.tickettools.utils.commands.ICommand;
import me.redstom.tickettools.utils.exceptions.TicketException;
import me.redstom.tickettools.utils.messages.Embeds;
import me.redstom.tickettools.utils.messages.builder.MessageFactory;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

@Command
@RequiredArgsConstructor
public class TicketCommand implements ICommand {

    private final TicketManager ticketManager;
    private final Embeds embeds;

    @Override
    public String getName() {
        return "ticket";
    }

    @Override
    public SlashCommandData getSlashCommandData() {
        return Commands.slash("ticket", "Ouvrir un ticket afin de communiquer avec la modération")
                .setDefaultPermissions(DefaultMemberPermissions.ENABLED);
    }

    @Override
    public void run(SlashCommandInteractionEvent event) throws TicketException, IOException {
        if (ticketManager.exists(event.getUser())) {
            // spotless:off
            MessageFactory.create()
                    .addEmbeds(embeds.ticketAlreadyExists(true))
                    .reply(event)
                    .queue();
            // spotless:on
            return;
        }

        InteractionHook reply = event.deferReply(true).complete();

        ticketManager.create(event.getUser()).proposeOpening(reply);
    }
}
