package yt.graven.gravensupport.ticket.channel;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.requests.RestAction;
import org.w3c.dom.Text;
import yt.graven.gravensupport.configuration.annotations.TicketCategory;
import yt.graven.gravensupport.ticket.Ticket;

import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
public class TicketChannel {

    private final Ticket source;

    @Getter
    private TextChannel channel;

    void create(@TicketCategory Category ticketCategory) {
        ticketCategory.createTextChannel(source.getUser().getName())
                .setTopic(computeTopic())
                .queue(this::setChannel);
    }

    private void setChannel(TextChannel channel) {
        this.channel = channel;
    }

    private String computeTopic() {
        StringBuilder builder = new StringBuilder("Ticket #").append(source.getId());

        if(source.getOpenedBy() != null) {
            builder.append(" - ")
                    .append("Ticket Staff");
        }

        builder.append(" - ").append(source.getOpeningReason());
        builder.append(" - ").append(source.getUser().getId());

        return builder.toString();
    }
}
