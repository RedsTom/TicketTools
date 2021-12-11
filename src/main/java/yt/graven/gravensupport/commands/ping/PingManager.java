package yt.graven.gravensupport.commands.ping;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.springframework.beans.factory.annotation.Autowired;
import yt.graven.gravensupport.Main;

import java.awt.*;

public class PingManager {

    @Autowired
    private Main main;

    public MessageEmbed compute() {
        return new EmbedBuilder()
            .setTitle(":ping_pong: Pong !")
            .setColor(Color.green)
            .addField("↔️ Ping du Gateway :", String.format("""
                    **`%s`** ms
                """.trim(), main.getClient().getGatewayPing()), false)
            .addField("➡️ Ping de l'API :", String.format("""
                    **`%s`** ms
                """.trim(), main.getClient().getRestPing().complete()), false)
            .build();
    }

}
