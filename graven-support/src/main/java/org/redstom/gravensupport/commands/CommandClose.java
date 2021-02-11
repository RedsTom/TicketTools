package org.redstom.gravensupport.commands;

import com.google.gson.GsonBuilder;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.event.message.MessageCreateEvent;
import org.redstom.botapi.server.IServer;
import org.redstom.gravensupport.listener.MessageListener;
import org.redstom.gravensupport.utils.Ticket;
import org.redstom.gravensupport.utils.TicketManager;
import org.redstom.gravensupport.utils.command.Command;
import org.redstom.gravensupport.utils.command.CommandExecutor;

import java.awt.*;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommandClose implements CommandExecutor {

    Pattern idPattern = Pattern.compile("[0-9]{18}");
    GsonBuilder builder = new GsonBuilder()
        .setPrettyPrinting();

    @Override
    public void run(IServer server, MessageCreateEvent event, Command command, String[] args) {
        if (!(event.getServer().get().getId() == MessageListener.SUPPORT_SERVER_ID)) {
            stop(event);
            return;
        }
        if (!event.getChannel().asServerTextChannel().isPresent()) {
            stop(event);
            return;
        }
        Matcher matcher = idPattern.matcher(event.getChannel().asServerTextChannel().get().getTopic());
        if (!matcher.find()) {
            stop(event);
            return;
        }
        if (event.getChannel().asServerChannel().get().asCategorizable().isEmpty()
            || event.getChannel().asServerChannel().get().asCategorizable().get().getCategory().isEmpty()) {
            stop(event);
            return;
        }
        if (event.getChannel().asServerChannel().get().asCategorizable().get().getCategory().get().getId()
            != MessageListener.TICKETS_CATEGORY_ID) {
            stop(event);
            return;
        }
        Optional<Ticket> ticket = TicketManager.getTicketByUserId(
            event.getChannel()
                .asServerTextChannel().get()
                .getTopic());
        if (ticket.isEmpty()) {
            stop(event);
            return;
        }
        Runnable runnable = () -> {
            ticket.get().close();
        };
        Thread thread = new Thread(runnable);
        thread.start();
    }

    private void stop(MessageCreateEvent event) {
        event.getChannel().sendMessage(new EmbedBuilder()
            .setColor(Color.RED)
            .setTitle("Commande inutilisable ici")
            .setDescription("Cette commande n'est pas utilisable dans ce salon !")
        );
    }
}
