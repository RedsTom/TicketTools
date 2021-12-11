package yt.graven.gravensupport;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.simpleyaml.configuration.file.YamlConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Component;
import yt.graven.gravensupport.utils.exceptions.TicketException;

import javax.security.auth.login.LoginException;
import java.util.EnumSet;
import java.util.List;

@Component
public class Main {

    public static void main(String[] args) throws LoginException {
        ApplicationContext context = new AnnotationConfigApplicationContext(BotConfig.class);

        Main main = context.getBean(Main.class);
        main.run();
    }


    private final EventReceiver eventReceiver;
    private final YamlConfiguration config;

    private JDA client;

    public Main(EventReceiver eventReceiver, YamlConfiguration config) {
        this.eventReceiver = eventReceiver;
        this.config = config;
    }

    private void run() throws LoginException {
        this.client = JDABuilder.create(EnumSet.allOf(GatewayIntent.class))
            .setToken(config.getString("config.token"))
            .build();

        this.client.getPresence().setPresence(
            Activity.listening(config.getString("config.prefix")
                + "new | Ouvrez un ticket avec la modération"),
            false
        );

        this.client.addEventListener(eventReceiver);
    }

    public JDA getClient() {
        return this.client;
    }
}