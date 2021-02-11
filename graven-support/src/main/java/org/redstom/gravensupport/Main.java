package org.redstom.gravensupport;

import org.javacord.api.entity.activity.ActivityType;
import org.redstom.botapi.BotPlugin;
import org.redstom.botapi.injector.Inject;
import org.redstom.botapi.server.IServer;
import org.redstom.gravensupport.utils.TicketManager;

@BotPlugin(author = "RedsTom", id = "Graven Support", name = "Graven Support")
public class Main {

    public static void unload() {

    }

    @Inject({IServer.class})
    public void load(IServer server) {
        server.getApi().updateActivity(ActivityType.LISTENING, server.getPrefix() + "new | Support en MP");
        server.getLogger().info("Plugin Graven Support chargé !");

        TicketManager.loadAllCurrentTickets(server.getApi());
    }

}
