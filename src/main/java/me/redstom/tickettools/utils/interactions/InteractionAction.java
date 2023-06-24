package me.redstom.tickettools.utils.interactions;

import java.io.IOException;
import me.redstom.tickettools.utils.exceptions.TicketException;
import net.dv8tion.jda.api.events.Event;

public interface InteractionAction<T extends Event> {

    void run(T event) throws TicketException, IOException;
}
