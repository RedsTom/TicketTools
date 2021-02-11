package org.redstom.botapi.events;

import org.javacord.api.event.Event;

/**
 * The event dispatcher of the server
 */
public interface IEventManager {

    /**
     * Dispatch an event to all the plugins
     *
     * @param event Event to dispatch
     * @param <T>   Kind of event to dispatch
     */
    <T extends Event> void dispatch(T event);

    /**
     * Registers an event subscriber class
     *
     * @param eventSubscriberClass The class to register
     */
    void register(Object eventSubscriberClass);

}
