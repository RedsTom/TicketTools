package yt.graven.gravensupport.common.exception;

public class BotStartupException extends RuntimeException {

  private static final String MESSAGE = "Unable to start bot: %s";

  public BotStartupException(String reason) {
    super(String.format(MESSAGE, reason));
  }

  public BotStartupException(String reason, Throwable cause) {
    super(String.format(MESSAGE, reason), cause);
  }
}
