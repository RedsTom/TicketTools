package org.redstom.gravensupport.utils;

import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.ChannelCategory;
import org.javacord.api.entity.channel.ServerChannel;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.redstom.botapi.server.IServer;
import org.redstom.gravensupport.listener.MessageListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class TicketManager {


    private static final List<Ticket> tickets = new ArrayList<>();

    public static List<Ticket> getAllTickets() {
        return tickets;
    }

    public static Ticket addTicket(Ticket ticket) {
        tickets.add(ticket);
        return ticket;
    }

    public static void removeTicket(Ticket ticket) {
        tickets.remove(ticket);
    }

    public static Optional<Ticket> getTicketByUserId(String id) {
        Optional<Ticket> ticket = tickets
            .stream()
            .filter(tk -> {
                boolean a = tk.getFrom().getIdAsString().equals(id);
                return a;
            })
            .findFirst();
        return ticket;
    }

    public static CompletableFuture<Ticket> createTicket(IServer server, User user) {
        return createTicket(server, user, true, false);
    }

    public static CompletableFuture<Ticket> createTicket(IServer server, User user, boolean dm) {
        return createTicket(server, user, dm, false);
    }

    public static CompletableFuture<Ticket> createTicket(IServer iServer, User user, boolean dm, boolean forced) {
        CompletableFuture<Ticket> future = new CompletableFuture<>();
        Server server = user.getApi().getServerById(MessageListener.SUPPORT_SERVER_ID)
            .get();
        ChannelCategory category = server.getChannelCategoryById(MessageListener.TICKETS_CATEGORY_ID)
            .get();
        loadAllCurrentTickets(user.getApi()).thenAccept(v -> {
            if (getTicketByUserId(user.getIdAsString()).isPresent() && dm) {
                Ticket ticket = getTicketByUserId(user.getIdAsString()).get();
                if (!forced) {
                    ticket.sendOpeningDM(iServer);
                } else {
                    ticket.sendForcedOpeningDm(iServer);
                }
                future.complete(getTicketByUserId(user.getIdAsString()).get());
            } else {
                server.createTextChannelBuilder()
                    .setCategory(category)
                    .setName(user.getName().replace(" ", "-"))
                    .create().thenAccept(channel -> {
                    Ticket ticket = addTicket(new Ticket(user, channel));
                    ticket.setupTicket().thenAccept((a) -> {
                        if (!forced) {
                            ticket.sendOpeningAnnounce(iServer, dm, forced);
                        } else {
                            ticket.sendOpeningAnnounce(iServer, false, forced);
                            ticket.sendForcedOpeningDm(iServer);
                        }
                        future.complete(ticket);
                    });
                });
            }
        });
        return future;
    }

    public static CompletableFuture<Void> loadAllCurrentTickets(DiscordApi api) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        tickets.clear();
        Server server = api.getServerById(MessageListener.SUPPORT_SERVER_ID)
            .get();
        ChannelCategory category = server.getChannelCategoryById(MessageListener.TICKETS_CATEGORY_ID)
            .get();
        for (int i = 0; i < category.getChannels().size(); i++) {
            ServerChannel channel = category.getChannels().get(i);
            if (!channel.asServerTextChannel().isPresent()) continue;
            api.getUserById(channel.asServerTextChannel().get().getTopic()).thenAccept(target -> {
                Ticket ticket = addTicket(new Ticket(target, channel.asServerTextChannel().get()));
                channel.asServerTextChannel().get().getWebhooks().thenAccept(webhooks -> {
                    if (!webhooks.get(0).asIncomingWebhook().isPresent()) {
                        ticket.setupTicket();
                    } else {
                        ticket.setWebhook(webhooks.get(0).asIncomingWebhook().get());
                    }
                });
            });
            if (i == (category.getChannels().size() - 1)) future.complete(null);
        }
        return future;
    }

}
