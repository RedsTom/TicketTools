package org.redstom.gravensupport.commands;

import org.javacord.api.DiscordApi;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.message.MessageCreateEvent;
import org.redstom.botapi.server.IServer;
import org.redstom.gravensupport.utils.TicketManager;
import org.redstom.gravensupport.utils.command.Command;
import org.redstom.gravensupport.utils.command.CommandExecutor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommandNew implements CommandExecutor {

    private final Pattern mentionPattern = Pattern.compile("<@![0-9]{18}>");
    private final Pattern idPattern = Pattern.compile("[0-9]{18}");

    @Override
    public void run(IServer server, MessageCreateEvent event, Command command, String[] args) {
        System.out.println(Arrays.toString(args));
        if (args.length == 0) {
            event.getMessageAuthor().asUser().ifPresent(user -> {
                event.getMessage().addReaction("✏");
                TicketManager.createTicket(server, user);
            });
        } else {
            if (event.getMessageAuthor().canManageServer()) {
                getUsersFromArgs(event.getApi(), args).thenAccept(users -> {
                    event.getMessage().addReaction("✏");
                    users.forEach(user -> {
                        TicketManager.createTicket(server, user, true, true);
                    });
                });
            }
        }
    }

    private CompletableFuture<List<User>> getUsersFromArgs(DiscordApi api, String[] args) {
        CompletableFuture<List<User>> future = new CompletableFuture<>();
        Thread th = new Thread(() -> {
            List<User> users = new ArrayList<>();
            for (int i = 0; i < args.length; i++) {
                String input = args[i];
                Matcher id = idPattern.matcher(input);
                Matcher mention = mentionPattern.matcher(input);
                if (id.find()) {
                    input = input.substring(id.start(), id.end());
                } else if (mention.find()) {
                    input = input.substring(mention.start() + 2, mention.end() - 1);
                }
                int finalI = i;
                api.getUserById(input).thenAccept(fUser -> {
                    if (fUser == null) return;
                    users.add(fUser);
                    if (finalI == (args.length - 1)) {
                        future.complete(users);
                    }
                });
            }
        });
        th.start();
        return future;
    }
}
