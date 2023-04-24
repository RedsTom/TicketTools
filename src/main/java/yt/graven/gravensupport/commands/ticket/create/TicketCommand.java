package yt.graven.gravensupport.commands.ticket.create;

import java.io.IOException;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import yt.graven.gravensupport.commands.ticket.TicketManager;
import yt.graven.gravensupport.utils.commands.Command;
import yt.graven.gravensupport.utils.commands.ICommand;
import yt.graven.gravensupport.utils.exceptions.TicketException;
import yt.graven.gravensupport.utils.messages.Embeds;
import yt.graven.gravensupport.utils.messages.builder.MessageFactory;

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
