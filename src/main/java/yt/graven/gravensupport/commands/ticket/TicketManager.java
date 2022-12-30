package yt.graven.gravensupport.commands.ticket;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import org.simpleyaml.configuration.file.YamlConfiguration;
import org.springframework.stereotype.Component;
import yt.graven.gravensupport.utils.exceptions.TicketAlreadyExistsException;
import yt.graven.gravensupport.utils.exceptions.TicketException;
import yt.graven.gravensupport.utils.messages.Embeds;

@Component
@RequiredArgsConstructor
public class TicketManager {

  private final YamlConfiguration config;
  private final Embeds embeds;

  private final Map<Long, Ticket> tickets = new HashMap<>();

  public Ticket create(User user) throws TicketException {
    if (exists(user)) {
      throw new TicketAlreadyExistsException(user);
    }
    Ticket t = new Ticket(this, embeds, config, user);
    store(t);
    return t;
  }

  public Ticket create(JDA jda, long userId) throws TicketException {
    User user = jda.retrieveUserById(userId).complete();
    return create(user);
  }

  public Optional<Ticket> get(User user) {
    return get(user.getIdLong());
  }

  public Optional<Ticket> get(long userId) {
    return Optional.ofNullable(tickets.get(userId));
  }

  public Ticket getOrCreate(User user) throws TicketException {
    return get(user).orElse(create(user));
  }

  public Ticket getOrCreate(JDA jda, long userId) throws TicketException {
    return get(userId).orElse(create(jda, userId));
  }

  public boolean exists(User user) {
    return tickets.containsKey(user.getIdLong());
  }

  public boolean exists(long userId) {
    return tickets.containsKey(userId);
  }

  public void store(Ticket ticket) {
    tickets.put(ticket.getFrom().getIdLong(), ticket);
  }

  public void load(JDA jda) throws TicketException {
    String guildId = config.getString("config.ticket_guild.guild_id");
    String categoryId = config.getString("config.ticket_guild.tickets_category");

    Guild guild = jda.getGuildById(guildId);

    if (guild == null) {
      throw new TicketException("Cannot retrieve guild with id " + guildId);
    }

    Category cat = guild.getCategoryById(categoryId);

    if (cat == null) {
      throw new TicketException(
          "Cannot retrieve the category with id " + categoryId + " in server " + guildId);
    }

    cat.getTextChannels()
        .forEach(
            channel -> {
              try {
                store(Ticket.loadFromChannel(this, embeds, config, channel));
              } catch (IOException e) {
                new TicketException("Impossible to load ticket from channel #" + channel.getName())
                    .printStackTrace();
              }
            });
  }

  public void remove(User user) {
    this.tickets.remove(user.getIdLong());
  }

  public void remove(long userId) {
    this.tickets.remove(userId);
  }
}
