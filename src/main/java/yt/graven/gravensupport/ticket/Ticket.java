package yt.graven.gravensupport.ticket;

import lombok.Builder;
import lombok.Value;

import java.time.Instant;

@Builder(toBuilder = true)
public @Value class Ticket {
    long id;

    long userId;
    String username;

    long channelId;

    long openingMessageId;
    long closingMessageId;

    TicketState status;

    Instant createdAt;
    Instant closedAt;

    long openedBy;
}
