package yt.graven.gravensupport;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.utils.MiscUtil;
import org.jetbrains.annotations.NotNull;
import org.simpleyaml.configuration.file.YamlConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import yt.graven.gravensupport.commands.ping.PingCommand;
import yt.graven.gravensupport.commands.ticket.Ticket;
import yt.graven.gravensupport.commands.ticket.TicketManager;
import yt.graven.gravensupport.commands.ticket.close.CloseCommand;
import yt.graven.gravensupport.commands.ticket.create.TicketCommand;
import yt.graven.gravensupport.commands.ticket.id.IdCommand;
import yt.graven.gravensupport.utils.commands.CommandRegistry;
import yt.graven.gravensupport.utils.exceptions.CommandCancelledException;
import yt.graven.gravensupport.utils.exceptions.TicketException;
import yt.graven.gravensupport.utils.interactions.ButtonActions;
import yt.graven.gravensupport.utils.interactions.ModalActions;
import yt.graven.gravensupport.utils.interactions.SelectionMenuActions;
import yt.graven.gravensupport.utils.messages.Embeds;

import java.io.IOException;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventReceiver extends ListenerAdapter {

    private final ApplicationContext context;
    private final CommandRegistry registry;
    private final PingCommand pingCommand;
    private final TicketCommand ticketCommand;
    private final CloseCommand closeCommand;
    private final IdCommand idCommand;
    private final YamlConfiguration config;
    private final TicketManager ticketManager;
    private final Embeds embeds;

    private boolean loaded = false;

    @Override
    public void onReady(@NotNull ReadyEvent event) {
        if (loaded) return;

        log.info("Bot preparing - Initializing commands...");
        registry.loadAll();
        log.info("Bot ready - Commands initialized!");

        try {
            ticketManager.load(event.getJDA());
        } catch (TicketException e) {
            e.printStackTrace();
        }

        loaded = true;
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        registry.getCommandByName(event.getFullCommandName())
                .ifPresent(cmd -> {
                    try {
                        cmd.run(event);
                    } catch (TicketException | IOException | CommandCancelledException e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (event.getAuthor().isBot() || event.getAuthor().isSystem()) return;

        /*
         * Check for a dm from a member for a potential forward to a ticket.
         */
        if (event.getChannelType() == ChannelType.PRIVATE) {
            if (!ticketManager.exists(event.getAuthor())) return;
            Ticket ticket = ticketManager.get(event.getAuthor()).get();

            if (!ticket.isOpened()) return;

            ticket.sendToTicket(event.getMessage());
            return;
        }

        /*
         * Check for a message in a ticket channel
         */
        if (event.getChannelType() == ChannelType.TEXT) {

            if (event.getMessage().getContentRaw().length() == 0)
                return;

            if (!event.getMessage().getContentRaw().startsWith("'"))
                return;

            TextChannel textChannel = event.getChannel().asTextChannel();
            if (!Objects.equals(textChannel.getParentCategoryId(), config.getString("config.ticket_guild.tickets_category")))
                return;

            Optional<Ticket> ticket = ticketManager.get(MiscUtil.parseLong(textChannel.getTopic()));
            if (ticket.isEmpty()) {
                embeds.noTicketAttachedMessage()
                        .actionRow()
                        .deletable()
                        .build()
                        .sendMessage(event.getChannel())
                        .queue();
                return;
            }

            ticket.get().sendToUser(event.getMessage());
        }
    }

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        ButtonActions.getFromActionId(event.getButton().getId())
                .ifPresent(a -> {
                    try {
                        a.run(context, event);
                    } catch (TicketException | IOException e) {
                        e.printStackTrace();
                    }
                });
    }

    @Override
    public void onStringSelectInteraction(@NotNull StringSelectInteractionEvent event) {
        SelectionMenuActions.getFromActionId(event.getSelectMenu().getId())
                .ifPresent(a -> {
                    try {
                        a.run(context, event);
                    } catch (TicketException | IOException e) {
                        e.printStackTrace();
                    }
                });
    }

    @Override
    public void onModalInteraction(@NotNull ModalInteractionEvent event) {
        ModalActions.getFromActionId(event.getModalId())
                .ifPresent(a -> {
                    try {
                        a.run(context, event);
                    } catch (TicketException | IOException e) {
                        e.printStackTrace();
                    }
                });
    }
}
