package yt.graven.gravensupport.utils.exceptions;

import net.dv8tion.jda.api.entities.User;

public class TicketAlreadyExistsException extends RuntimeException {

  public TicketAlreadyExistsException(User user) {
    super(String.format("A ticket for the user %s is already opened", user.getAsTag()));
  }
}
