package org.redstom.gravensupport.listener;

import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.event.user.UserStartTypingEvent;
import org.redstom.botapi.events.EventBus;
import org.redstom.botapi.events.EventReceiver;
import org.redstom.botapi.events.SelfRegisteringListener;
import org.redstom.botapi.injector.Inject;
import org.redstom.botapi.server.IServer;
import org.redstom.gravensupport.commands.CommandClose;
import org.redstom.gravensupport.commands.CommandNew;
import org.redstom.gravensupport.commands.CommandPing;
import org.redstom.gravensupport.utils.Ticket;
import org.redstom.gravensupport.utils.TicketManager;
import org.redstom.gravensupport.utils.command.Command;
import org.redstom.gravensupport.utils.command.CommandRegistry;

import java.util.Arrays;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SelfRegisteringListener
@EventBus
public class MessageListener {

    public static final long SUPPORT_SERVER_ID = 806257464986697728L;
    public static final long REPORTS_CHANNEL_ID = 808421751964303380L;
    public static final long TICKETS_CHANNEL_ID = 806257832717451305L;
    public static final long ATTACHEMENTS_CHANNEL_ID = 808738829690732564L;
    public static final long TICKETS_CATEGORY_ID = 806258034242224178L;
    public static final long REACTION_ID = 808731655060979743L;

    private final CommandRegistry registry = new CommandRegistry();
    Pattern idPattern = Pattern.compile("[0-9]{18}");

    public MessageListener() {
        registry.addCommand(new Command(
            "ping",
            "Pings the bot",
            new CommandPing(),
            "ping", "p?"
        ));
        registry.addCommand(new Command(
            "new",
            "Creates a new ticket",
            new CommandNew(),
            "new", "create", "ticket", "support"
        ));
        registry.addCommand(new Command(
            "close",
            "Closes the current ticket",
            new CommandClose(),
            "close", "stop", "ticket-close", "support-close"
        ));
    }

    @Inject({IServer.class})
    @EventReceiver(MessageCreateEvent.class)
    public void onMessage(MessageCreateEvent event, IServer server) {
        if (!event.getMessageAuthor().isRegularUser()) return;
        if (event.getChannel().asServerTextChannel().isPresent()) {
            if (event.getChannel().asServerChannel().get().asCategorizable().isPresent()
                && event.getChannel().asServerChannel().get().asCategorizable().get().getCategory().isPresent()) {
                Matcher idMatcher = idPattern.matcher(event.getChannel().asServerTextChannel().get().getTopic());
                if (idMatcher.find()
                    && event.getServer().get().getId() == SUPPORT_SERVER_ID
                    && event.getChannel()
                    .asServerChannel().get()
                    .asCategorizable().get()
                    .getCategory().get()
                    .getId() == TICKETS_CATEGORY_ID
                    && event.getMessage().getContent().startsWith("> ")) {
                    Optional<Ticket> ticket = TicketManager
                        .getTicketByUserId(event.getChannel().asServerTextChannel().get().getTopic());
                    if (ticket.isPresent()) {
                        MessageBuilder builder = MessageBuilder.fromMessage(event.getMessage());
                        builder.setContent(event.getMessageContent().substring(2));
                        ticket.get().sendToUser(builder);
                        event.getMessage().addReaction(
                            event.getApi().getServerById(SUPPORT_SERVER_ID).get().getCustomEmojiById(REACTION_ID).get()
                        );
                    }
                }
            }
        }
        if (event.isPrivateMessage()) {
            Optional<Ticket> ticket = TicketManager
                .getTicketByUserId(event.getMessageAuthor().getIdAsString());
            event.getMessage().addReaction(
                event.getApi().getServerById(SUPPORT_SERVER_ID).get().getCustomEmojiById(REACTION_ID).get()
            );
            ticket.get().sendToTicket(server, event.getApi(), MessageBuilder.fromMessage(event.getMessage()));
        } else {
            // If message is on server : Execute as command
            if (event.getMessageContent().startsWith(server.getPrefix())) {
                String[] args = event.getMessageContent().split(" ");
                String cmdName = args[0].substring(server.getPrefix().length());
                args = args.length == 1 ? new String[0] : Arrays.copyOfRange(args, 1, args.length - 1);

                String[] finalArgs = args;
                registry.getByAlias(cmdName).ifPresent(cmd -> cmd.getExecutor().run(server, event, cmd, finalArgs));
            }
        }
    }

    @EventReceiver(UserStartTypingEvent.class)
    public void onStartTyping(UserStartTypingEvent event) {
        if (event.getChannel().asServerTextChannel().isEmpty()) return;
        if (event.getChannel().asServerTextChannel().get().getServer().getId() != SUPPORT_SERVER_ID) return;
        Matcher idMatcher = idPattern.matcher(event.getChannel().asServerTextChannel().get().getName());
        if (!idMatcher.find()) {
            return;
        }
        Optional<Ticket> ticket = TicketManager
            .getTicketByUserId(event.getChannel().asServerTextChannel().get().getTopic());
        if (ticket.isEmpty()) return;
        ticket.get().getFrom().getPrivateChannel().get().type();
        if (ticket.get().getFrom().getPrivateChannel().isEmpty()) {
            ticket.get().getFrom().sendMessage("Opening conversation...").thenAccept(Message::delete);
        }
        ticket.get().getFrom().getPrivateChannel().get().type();
    }
}
