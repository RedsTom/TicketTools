package yt.graven.gravensupport.commands.ticket.create;

import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import yt.graven.gravensupport.commands.ticket.OldTicket;
import yt.graven.gravensupport.commands.ticket.OldTicketManager;
import yt.graven.gravensupport.utils.commands.Command;
import yt.graven.gravensupport.utils.commands.ICommand;
import yt.graven.gravensupport.utils.exceptions.CommandCancelledException;
import yt.graven.gravensupport.utils.exceptions.TicketException;
import yt.graven.gravensupport.utils.messages.Embeds;

import java.io.IOException;
import java.util.Arrays;

import static net.dv8tion.jda.api.entities.channel.ChannelType.*;

@Command
@RequiredArgsConstructor
public class ModTicketCommand implements ICommand {

    private final Embeds embeds;
    private final OldTicketManager oldTicketManager;

    @Override
    public String getName() {
        return "modticket";
    }

    @Override
    public SlashCommandData getSlashCommandData() {
        return Commands.slash("modticket", "Gestion des tickets par les modérateurs")
                .setGuildOnly(true)
                .setDefaultPermissions(DefaultMemberPermissions.DISABLED)
                .addSubcommands(
                        new SubcommandData("open-with", "Ouvrir un ticket avec un utilisateur en particulier")
                                .addOption(OptionType.USER, "user", "L'utilisateur avec qui ouvrir le ticket", true),
                        new SubcommandData("refresh-open", "Rafraichir les tickets ouverts"),
                        new SubcommandData("force-close", "Forcer le bot à croire que le ticket est fermé")
                                .addOption(
                                        OptionType.USER, "user", "L'utilisateur dont le ticket doit être fermé", true));
    }

    @Override
    public void run(SlashCommandInteractionEvent event) throws TicketException, IOException, CommandCancelledException {
        switch (event.getSubcommandName()) {
            case "open-with" -> this.runWithSelectedUser(event);
            default -> event.reply("Cette commande n'est pas encore implémentée.")
                    .setEphemeral(true)
                    .queue();
        }
    }

    private void runWithSelectedUser(SlashCommandInteractionEvent event) throws IOException, TicketException {
        InteractionHook reply = event.deferReply(true).complete();
        if (!Arrays.asList(TEXT, GUILD_PRIVATE_THREAD, GUILD_PUBLIC_THREAD).contains(event.getChannelType())) {
            embeds.errorMessage("Cette commande doit être exécutée sur un serveur.")
                    .editReply(reply)
                    .queue();
            return;
        }

        assert event.getMember() != null;
        if (!event.getMember().hasPermission(Permission.BAN_MEMBERS)) {
            embeds.errorMessage("Vous n'avez pas la permission pour exécuter cette commande (`BAN_MEMBERS`).")
                    .editReply(reply)
                    .queue();
            return;
        }

        OptionMapping userOption = event.getOption("user");

        if (userOption == null) {
            embeds.errorMessage("L'option `%s` est obligatoire.".formatted("user"))
                    .editReply(reply)
                    .queue();
            return;
        }

        User user = userOption.getAsUser();

        if (oldTicketManager.exists(user)) {
            embeds.ticketAlreadyExistsMessage(oldTicketManager.get(user).get().getTo(), false)
                    .editReply(reply)
                    .queue();
            return;
        }

        if (user.getMutualGuilds().isEmpty()) {
            embeds.errorMessage(String.format(
                            "L'utilisateur %s n'a aucun serveur en commun avec le bot de ticket.", user.getAsTag()))
                    .editReply(reply)
                    .queue();
            return;
        }

        OldTicket ticket = oldTicketManager.create(user);
        ticket.forceOpening(event.getUser());

        // spotless::off
        embeds.successMessage(String.format("Le ticket avec %s a bien été ouvert.", user.getAsMention()))
                .addActionRow(actionRow -> actionRow.addButton(button ->
                        button.setText("Aller au ticket").setLink(ticket.getTo().getJumpUrl())))
                .editReply(reply)
                .queue();
        // spotless:on
    }
}
