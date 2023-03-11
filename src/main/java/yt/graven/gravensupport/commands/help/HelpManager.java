package yt.graven.gravensupport.commands.help;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.simpleyaml.configuration.file.YamlConfiguration;
import org.springframework.stereotype.Component;
import yt.graven.gravensupport.utils.commands.CommandRegistry;
import yt.graven.gravensupport.utils.commands.ICommand;

@Component
@RequiredArgsConstructor
public class HelpManager {

  private final CommandRegistry registry;
  private final YamlConfiguration config;

  private final HashMap<Long, Integer> page = new HashMap<>();

  private int maxPage;
  private MessageEmbed[] embeds;

  private static final int RESULT_PER_PAGE = 1;
  private static final EmbedBuilder DEFAULT_HELP_EMBED =
      new EmbedBuilder()
          .setTitle("Besoin d'aide ?")
          .setDescription("Voici la liste des commandes disponibles avec le bot de tickets :")
          .setColor(Color.green);

  private void init() {
    int size = registry.getShownCommands().size();
    this.maxPage = (int) Math.ceil((0d + size) / RESULT_PER_PAGE);
  }

  public void updateEmbeds() {
    init();

    List<EmbedBuilder> builders = new ArrayList<>();

    for (int i = 0; i < registry.getShownCommands().size(); i++) {
      int page = (int) Math.floor((0d + i) / RESULT_PER_PAGE) + 1;

      if (page != builders.size() || builders.size() == 0) {
        EmbedBuilder newBuilder = new EmbedBuilder();
        newBuilder.copyFrom(DEFAULT_HELP_EMBED);
        newBuilder.setFooter("Page " + page + "/" + maxPage);

        builders.add(newBuilder);
      }

      ICommand cmd = registry.getShownCommands().get(i);
      EmbedBuilder builder =
          builders
              .get(builders.size() - 1)
              .addField(
                  "`" + config.getString("config.prefix") + cmd.getNames()[0] + "`",
                  cmd.getDescription()
                      + "\n\n**Autres noms** : `"
                      + String.join("`, `", cmd.getNames())
                      + "`",
                  true);
    }

    this.embeds = builders.stream().map(EmbedBuilder::build).toArray(MessageEmbed[]::new);
  }

  public MessageEmbed getPrevPage(long msgId) {
    page.put(msgId, Math.max(page.getOrDefault(msgId, 1) - 1, 1));
    return getCurrentPage(msgId);
  }

  public MessageEmbed getCurrentPage(long msgId) {
    return this.embeds[page.getOrDefault(msgId, 1) - 1];
  }

  public MessageEmbed getCurrentPage() {
    return getCurrentPage(-1);
  }

  public MessageEmbed getNextPage(long msgId) {
    page.put(msgId, Math.min(page.getOrDefault(msgId, 1) + 1, maxPage));
    return getCurrentPage(msgId);
  }

  public int getPage(long msgId) {
    return page.getOrDefault(msgId, 1);
  }

  public int getMaxPage() {
    return maxPage;
  }
}
