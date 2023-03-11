package yt.graven.gravensupport.configuration;

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
import yt.graven.gravensupport.common.exception.BotStartupException;
import yt.graven.gravensupport.common.exception.ImpossibleException;
import yt.graven.gravensupport.configuration.exception.ConfigurationException;

@Configuration
@ComponentScan("yt.graven.gravensupport")
public class BotConfig {
  private static final String CONFIGURATION_FILE = "config.yml";
  private static final String DEFAULT_CONFIGURATION_FILE = "config.default.yml";
  private static final String CONFIGURATION_TOKEN_PROPERTY = "config.token";

  @Bean
  public YamlConfiguration getBotConfiguration() {
    Path configurationFile = Paths.get(CONFIGURATION_FILE);

    if (!Files.exists(configurationFile)) {
      try {
        Files.createFile(configurationFile);
        Path defaultConfigFile = getDefaultConfigFromInsideJar();
        String defaultConfigurationData =
            String.join(System.lineSeparator(), Files.readAllLines(defaultConfigFile));
        Files.writeString(configurationFile, defaultConfigurationData);
      } catch (IOException exception) {
        throw new ConfigurationException("Unable to create default configuration file!", exception);
      }

      throw new ConfigurationException("Configuration file did not exist and has been created!");
    }

    return YamlConfiguration.loadConfiguration(configurationFile.toFile());
  }

  @Bean
  @SuppressWarnings("unused")
  public JDA getJDAInstance() {
    String token = this.getBotConfiguration().getString(CONFIGURATION_TOKEN_PROPERTY);
    if (token.isEmpty()) {
      throw new BotStartupException("No token provided!");
    }

    try {
      EnumSet<GatewayIntent> allIntentsBecauseWhyNot = EnumSet.allOf(GatewayIntent.class);
      return JDABuilder.create(allIntentsBecauseWhyNot).setToken(token).build();
    } catch (LoginException exception) {
      throw new BotStartupException(
          "Unable to start JDA instance. Please ensure your token is valid!", exception);
    }
  }

  private Path getDefaultConfigFromInsideJar() {
    try {
      URL resourceUrl =
          Optional.ofNullable(getClass().getClassLoader().getResource(DEFAULT_CONFIGURATION_FILE))
              .orElseThrow(
                  () ->
                      new ConfigurationException(
                          "Unable to retrieve default configuration file in JAR"));
      return Paths.get(resourceUrl.toURI());
    } catch (URISyntaxException e) {
      throw new ImpossibleException("a JAR file URL is a valid URI, wtf happened?");
    }
  }
}
