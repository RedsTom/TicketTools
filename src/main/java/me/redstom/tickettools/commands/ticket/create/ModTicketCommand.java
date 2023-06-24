package me.redstom.tickettools.commands.ticket.create;

import static net.dv8tion.jda.api.entities.channel.ChannelType.*;

import java.io.IOException;
import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import me.redstom.tickettools.commands.ticket.Ticket;
import me.redstom.tickettools.commands.ticket.TicketManager;
import me.redstom.tickettools.commands.ticket.TicketOpeningReason;
import me.redstom.tickettools.utils.commands.Command;
import me.redstom.tickettools.utils.commands.ICommand;
import me.redstom.tickettools.utils.exceptions.CommandCancelledException;
import me.redstom.tickettools.utils.exceptions.TicketException;
import me.redstom.tickettools.utils.messages.Embeds;
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

@Command
@RequiredArgsConstructor
public class ModTicketCommand implements ICommand {

    private final Embeds embeds;
    private final TicketManager ticketManager;

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
                                .addOption(OptionType.USER, "user", "L'utilisateur avec qui ouvrir le ticket", true)
                                .addOption(
                                        OptionType.STRING,
                                        "reason",
                                        "La raison pour laquelle le ticket est ouvert",
                                        true),
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
        OptionMapping reasonOption = event.getOption("reason");

        if (userOption == null) {
            embeds.errorMessage("L'option `%s` est obligatoire.".formatted("user"))
                    .editReply(reply)
                    .queue();
            return;
        }
        if (reasonOption == null) {
            embeds.errorMessage("L'option `%s` est obligatoire.".formatted("reason"))
                    .editReply(reply)
                    .queue();
            return;
        }

        User user = userOption.getAsUser();
        String reason = reasonOption.getAsString();

        if (ticketManager.exists(user)) {
            embeds.ticketAlreadyExistsMessage(ticketManager.get(user).get().getTo(), false)
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

        Ticket ticket = ticketManager.create(user);
        ticket.forceOpening(event.getUser(), new TicketOpeningReason.Simple(reason));

        // spotless::off
        embeds.successMessage(String.format("Le ticket avec %s a bien été ouvert.", user.getAsMention()))
                .addActionRow(actionRow -> actionRow.addButton(button ->
                        button.setText("Aller au ticket").setLink(ticket.getTo().getJumpUrl())))
                .editReply(reply)
                .queue();
        // spotless:on
    }
}
