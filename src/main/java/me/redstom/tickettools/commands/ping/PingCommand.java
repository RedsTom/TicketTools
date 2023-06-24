package me.redstom.tickettools.commands.ping;

import lombok.RequiredArgsConstructor;
import me.redstom.tickettools.utils.commands.Command;
import me.redstom.tickettools.utils.commands.ICommand;
import me.redstom.tickettools.utils.messages.Embeds;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

@Command
@RequiredArgsConstructor
public class PingCommand implements ICommand {
    private final PingComputer pingComputer;

    private final Embeds embeds;

    @Override
    public String getName() {
        return "ping";
    }

    @Override
    public SlashCommandData getSlashCommandData() {
        return Commands.slash("ping", "Calcule la latence du bot")
                .setDefaultPermissions(DefaultMemberPermissions.ENABLED);
    }

    @Override
    public void run(SlashCommandInteractionEvent event) {
        pingComputer
                .update()
                .thenAccept(ignored -> embeds.ping(pingComputer).reply(event).queue());
    }
}
