package yt.graven.gravensupport;

import java.io.*;
import java.util.EnumSet;
import java.util.Objects;
import javax.security.auth.login.LoginException;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.simpleyaml.configuration.file.YamlConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan("yt.graven.gravensupport")
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
      BufferedReader defaultConfig =
          new BufferedReader(
              new InputStreamReader(
                  Objects.requireNonNull(
                      getClass().getClassLoader().getResourceAsStream("config.default.yml"))));
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
  public JDA jda() throws LoginException, IOException {
    return JDABuilder.create(EnumSet.allOf(GatewayIntent.class))
        .setToken(this.config().getString("config.token"))
        .build();
  }
}
