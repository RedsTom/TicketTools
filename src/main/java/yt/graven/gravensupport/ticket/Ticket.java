package yt.graven.gravensupport.ticket;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Value;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import yt.graven.gravensupport.ticket.channel.TicketChannel;

import java.time.Instant;

@Builder(access = AccessLevel.PRIVATE)
public @Value class Ticket {

    long id;

    User user;
    TicketChannel channel;

    Message openingMessage;
    Message closingMessage;

    TicketState status;

    Instant createdAt;
    Instant closedAt;

    User openedBy;
    String openingReason;

    public RawTicket asRawTicket() {
        return RawTicket.builder()
                .id(id)
                .userId(user.getIdLong())
                .channelId(channel.getChannel().getIdLong())
                .openingMessageId(openingMessage.getIdLong())
                .closingMessageId(closingMessage.getIdLong())
                .status(status)
                .createdAt(createdAt)
                .closedAt(closedAt)
                .openedBy(openedBy.getIdLong())
                .openingReason(openingReason)
                .build();
    }
}
