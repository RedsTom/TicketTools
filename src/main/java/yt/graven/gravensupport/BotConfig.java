package yt.graven.gravensupport;

import net.dv8tion.jda.api.JDA;
import org.simpleyaml.configuration.file.YamlConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import yt.graven.gravensupport.commands.help.HelpCommand;
import yt.graven.gravensupport.commands.help.HelpManager;
import yt.graven.gravensupport.commands.help.interactions.NextPageHandler;
import yt.graven.gravensupport.commands.help.interactions.PrevPageHandler;
import yt.graven.gravensupport.commands.ping.PingCommand;
import yt.graven.gravensupport.commands.ping.PingManager;
import yt.graven.gravensupport.commands.ping.interactions.RefreshPingHandler;
import yt.graven.gravensupport.commands.ticket.TicketManager;
import yt.graven.gravensupport.commands.ticket.close.CloseCommand;
import yt.graven.gravensupport.commands.ticket.create.TicketCommand;
import yt.graven.gravensupport.commands.ticket.create.interactions.*;
import yt.graven.gravensupport.utils.commands.CommandRegistry;
import yt.graven.gravensupport.utils.exceptions.TicketException;
import yt.graven.gravensupport.utils.messages.Embeds;

import javax.security.auth.login.LoginException;
import java.io.*;
import java.util.Objects;

@Configuration
public class BotConfig {

    // GENERALITIES

    @Bean
    public File configurationFile() {
        return new File("config.yml");
    }

    @Bean
    public YamlConfiguration config() throws IOException {
        File configurationFile = configurationFile();
        if (!configurationFile.exists()) {
            configurationFile.createNewFile();
            FileWriter writer = new FileWriter(configurationFile);
            BufferedReader defaultConfig = new BufferedReader(new InputStreamReader(Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream("config.default.yml"))));
            while (defaultConfig.ready()) {
                writer.write(defaultConfig.readLine());
                writer.write("\n");
            }
            writer.flush();
            writer.close();

            throw new RuntimeException("Unable to start bot : Config did not exist !");
        }

        return YamlConfiguration.loadConfiguration(configurationFile);
    }

    @Bean
    public EventReceiver eventReceiver() {
        return new EventReceiver();
    }

    @Bean
    public CommandRegistry commandRegistry() {
        return new CommandRegistry();
    }

    @Bean
    public Embeds embeds() {
        return new Embeds();
    }

    @Bean
    public Main main() throws LoginException, IOException {
        return new Main(eventReceiver(), config());
    }

    @Bean
    public JDA jda() throws LoginException, IOException, TicketException {
        return main().getClient();
    }

    // HELP COMMAND

    @Bean
    public HelpCommand helpCommand() {
        return new HelpCommand();
    }

    @Bean
    public HelpManager helpManager() {
        return new HelpManager();
    }

    @Bean
    public PrevPageHandler prevPageHandler() {
        return new PrevPageHandler();
    }

    @Bean
    public NextPageHandler nextPageHandler() {
        return new NextPageHandler();
    }


    // PING COMMAND

    @Bean
    public PingCommand pingCommand() {
        return new PingCommand();
    }

    @Bean
    public PingManager pingManager() {
        return new PingManager();
    }

    @Bean
    public RefreshPingHandler refreshPingHandler() {
        return new RefreshPingHandler();
    }

    // TICKET COMMAND

    @Bean
    public TicketCommand ticketCommand() {
        return new TicketCommand();
    }

    @Bean
    public TicketManager ticketManager() {
        return new TicketManager();
    }

    @Bean
    public ValidateOpeningHandler validateOpeningHandler() {
        return new ValidateOpeningHandler();
    }

    @Bean
    public ConfirmMessageHandler confirmMessageHandler() {
        return new ConfirmMessageHandler();
    }

    @Bean
    public DeleteMessageHandler deleteMessageHandler() {
        return new DeleteMessageHandler();
    }

    @Bean
    public DenyMessageHandler denyMessageHandler() {
        return new DenyMessageHandler();
    }

    @Bean
    public FirstSentenceHandler firstSentenceHandler() {
        return new FirstSentenceHandler();
    }

    // CLOSE COMMAND

    @Bean
    public CloseCommand closeCommand() {
        return new CloseCommand();
    }

}
