package yt.graven.gravensupport;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.EnumSet;
import java.util.Optional;
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
  public static final String CONFIGURATION_FILE = "config.yml";
  public static final String DEFAULT_CONFIG_FILE = "config.default.yml";

  // GENERALITIES

  @Bean
  public YamlConfiguration config() throws IOException, URISyntaxException {
    Path configurationFile = Paths.get(CONFIGURATION_FILE);

    if (!Files.exists(configurationFile)) {
      Files.createFile(configurationFile);

      Path defaultConfigFile = getDefaultConfigFromInsideJar();
      String defaultConfigurationData = String.join(System.lineSeparator(), Files.readAllLines(defaultConfigFile));
      Files.writeString(configurationFile, defaultConfigurationData);

      throw new RuntimeException("Unable to start bot : Config did not exist !");
    }

    return YamlConfiguration.loadConfiguration(configurationFile.toFile());
  }

  @Bean
  public JDA jda() throws LoginException, IOException, URISyntaxException {
    return JDABuilder.create(EnumSet.allOf(GatewayIntent.class))
        .setToken(this.config().getString("config.token"))
        .build();
  }

  private Path getDefaultConfigFromInsideJar() throws URISyntaxException {
    URL resourceUrl = Optional.ofNullable(getClass().getClassLoader().getResource(DEFAULT_CONFIG_FILE)).orElseThrow(RuntimeException::new);
    return Paths.get(resourceUrl.toURI());
  }
}
