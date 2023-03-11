package yt.graven.gravensupport.configuration.exception;

import yt.graven.gravensupport.common.exception.BotStartupException;

public class ConfigurationException extends BotStartupException {

  public ConfigurationException(String reason) {
    super(reason);
  }

  public ConfigurationException(String reason, Throwable throwable) {
    super(reason, throwable);
  }
}
