package yt.graven.gravensupport.common.exception;

/** This exception should only be thrown on an impossible case scenario */
public class ImpossibleException extends RuntimeException {

  private static final String MESSAGE = "Weird exception: %s";

  public ImpossibleException(String reason) {
    super(String.format(MESSAGE, reason));
  }
}
