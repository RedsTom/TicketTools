package yt.graven.gravensupport.commands.ticket.id;

import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.utils.MiscUtil;
import org.simpleyaml.configuration.file.YamlConfiguration;
import yt.graven.gravensupport.commands.ticket.OldTicket;
import yt.graven.gravensupport.commands.ticket.OldTicketManager;
import yt.graven.gravensupport.utils.commands.Command;
import yt.graven.gravensupport.utils.commands.ICommand;
import yt.graven.gravensupport.utils.exceptions.CommandCancelledException;
import yt.graven.gravensupport.utils.exceptions.TicketException;
import yt.graven.gravensupport.utils.messages.Embeds;

import java.io.IOException;
import java.util.Objects;
import java.util.Optional;

@Command
@RequiredArgsConstructor
public class IdCommand implements ICommand {

    private final YamlConfiguration config;
    private final OldTicketManager oldTicketManager;
    private final Embeds embeds;

    @Override
    public String getName() {
        return "id";
    }

    @Override
    public SlashCommandData getSlashCommandData() {
        return Commands.slash("id", "Affiche l'ID du ticket actuel")
                .setGuildOnly(true)
                .setDefaultPermissions(DefaultMemberPermissions.DISABLED);
    }

    @Override
    public void run(SlashCommandInteractionEvent event) throws TicketException, IOException, CommandCancelledException {

        if (event.getChannelType() == ChannelType.PRIVATE) {
            throw new CommandCancelledException();
        }

        if (!event.getGuild().getId().equals(config.getString("config.ticket_guild.guild_id"))) {
            throw new CommandCancelledException();
        }

        TextChannel textChannel = event.getChannel().asTextChannel();
        if (!Objects.equals(
                textChannel.getParentCategoryId(), config.getString("config.ticket_guild.tickets_category"))) {
            embeds.errorMessage("Cette commande doit être exécutée dans un ticket !")
                    .reply(event)
                    .queue();
            return;
        }

        Optional<OldTicket> ticket = oldTicketManager.get(MiscUtil.parseLong(((TextChannel) event.getChannel()).getTopic()));
        if (ticket.isEmpty()) {
            embeds.errorMessage("Impossible de trouver le ticket associé à ce salon !")
                    .reply(event)
                    .queue();
            return;
        }

        event.reply(ticket.get().getFrom().getId()).queue();
    }
}
