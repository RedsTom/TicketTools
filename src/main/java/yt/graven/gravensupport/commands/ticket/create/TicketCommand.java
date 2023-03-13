package yt.graven.gravensupport.commands.ticket.create;

import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.utils.MiscUtil;
import org.springframework.stereotype.Component;
import yt.graven.gravensupport.commands.ticket.Ticket;
import yt.graven.gravensupport.commands.ticket.TicketManager;
import yt.graven.gravensupport.utils.commands.ICommand;
import yt.graven.gravensupport.utils.exceptions.TicketException;
import yt.graven.gravensupport.utils.messages.Embeds;
import yt.graven.gravensupport.utils.messages.TMessage;

import java.io.IOException;
import java.util.Arrays;

import static net.dv8tion.jda.api.entities.ChannelType.*;

@Component
@RequiredArgsConstructor
public class TicketCommand implements ICommand {

    private final TicketManager ticketManager;
    private final Embeds embeds;

    @Override
    public String[] getNames() {
        return new String[]{
            "new", "ticket"
        };
    }

    @Override
    public String getDescription() {
        return "Sert à ouvrir un ticket afin de communiquer avec la modération.";
    }

    @Override
    public void run(MessageReceivedEvent event, String[] args) throws TicketException, IOException {
        switch (args.length) {
            case 0 -> this.runDefault(event);
            case 1 -> this.runWithSelectedUser(event, args);
            default -> this.syntaxError(event, args);
        }
    }

    private void runDefault(MessageReceivedEvent event) throws TicketException {
        if (ticketManager.exists(event.getAuthor())) {
            TMessage.from(embeds.ticketAlreadyExists(true))
                .actionRow()
                .deletable()
                .build()
                .sendMessage(event.getChannel())
                .queue();
            return;
        }

        ticketManager.create(event.getAuthor()).proposeOpening(event.getChannel());
    }

    private void runWithSelectedUser(MessageReceivedEvent event, String[] args) throws TicketException, IOException {
        if (!Arrays.asList(TEXT, GUILD_PRIVATE_THREAD, GUILD_PUBLIC_THREAD).contains(event.getChannelType())) {
            embeds
                .errorMessage("Cette commande doit être exécutée sur un serveur.")
                .sendMessage(event.getChannel())
                .queue();
            return;
        }

        assert event.getMember() != null;
        if (!event.getMember().hasPermission(Permission.BAN_MEMBERS)) {
            embeds
                .errorMessage("Vous n'avez pas la permission pour exécuter cette commande (`BAN_MEMBERS`).")
                .sendMessage(event.getChannel())
                .queue();
            return;
        }

        String userId = args[0];
        try {
            MiscUtil.parseSnowflake(userId);
        } catch (NumberFormatException e) {
            embeds
                .errorMessage(String.format(
                    "L'identifiant `%s` est invalide.",
                    userId
                ))
                .sendMessage(event.getChannel())
                .queue();
            return;
        }

        User user = event.getJDA().retrieveUserById(userId).complete();

        if (user == null) {
            embeds
                .errorMessage(String.format(
                    "`Le membre avec l'identifiant `%s` ne peut être trouvé.",
                    userId
                ))
                .sendMessage(event.getChannel())
                .queue();
            return;
        }

        if (ticketManager.exists(user)) {
            embeds
                .ticketAlreadyExistsMessage(ticketManager.get(user).get().getTo(), false)
                .sendMessage(event.getChannel())
                .queue();
            return;
        }

        if (user.getMutualGuilds().size() == 0) {
            embeds
                .errorMessage(String.format("L'utilisateur %s n'a aucun serveur en commun avec le bot de ticket.", user.getAsTag()))
                .sendMessage(event.getChannel())
                .queue();
            return;
        }


        Ticket ticket = ticketManager.create(user);
        ticket.forceOpening(event.getAuthor());

        embeds
            .successMessage(String.format(
                "Le ticket avec %s a bien été ouvert.",
                user.getName()
            ))
            .sendMessage(event.getChannel())
            .queue();
    }

    private void syntaxError(MessageReceivedEvent event, String[] args) {
        embeds.errorMessage(String.format("""
                0 ou 1 arguments attendus, %s reçus !
                                        
                Syntaxe de la commande : `new [id]`
                """, args.length))
            .sendMessage(event.getChannel())
            .queue();
    }

}
