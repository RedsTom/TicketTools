package org.redstom.botapi.server;

import org.apache.logging.log4j.Logger;
import org.javacord.api.DiscordApi;
import org.redstom.botapi.events.IEventManager;

/**
 * Server that host the plugin
 */
public interface IServer {

    /**
     * @return The event dispatcher of the server
     */
    IEventManager getEventManager();

    /**
     * @return If the server is actually started
     */
    boolean isServerStarted();

    /**
     * @return If the server is actually stopping
     */
    boolean isStopping();

    /**
     * @return The DiscordBot instance
     */
    DiscordApi getApi();

    /**
     * @return The logger to log informations
     */
    Logger getLogger();

    /**
     * @return The prefix of the bot
     */
    String getPrefix();
}
