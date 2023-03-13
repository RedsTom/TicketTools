package yt.graven.gravensupport.utils.interactions;

import net.dv8tion.jda.api.events.Event;
import yt.graven.gravensupport.utils.exceptions.TicketException;

import java.io.IOException;

public interface IIInteractionAction<T extends Event> {

    void run(T event) throws TicketException, IOException;

}
